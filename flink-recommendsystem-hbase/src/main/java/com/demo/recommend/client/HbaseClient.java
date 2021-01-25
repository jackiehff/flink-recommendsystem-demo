package com.demo.recommend.client;

import com.demo.recommend.util.Property;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HbaseClient {
    private static Admin admin;
    public static Connection conn;

    static {
        Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.rootdir", Property.getStrValue("hbase.rootdir"));
        conf.set("hbase.zookeeper.quorum", Property.getStrValue("hbase.zookeeper.quorum"));
        conf.set("hbase.client.scanner.timeout.period", Property.getStrValue("hbase.client.scanner.timeout.period"));
        conf.set("hbase.rpc.timeout", Property.getStrValue("hbase.rpc.timeout"));
        try {
            conn = ConnectionFactory.createConnection(conf);
            admin = conn.getAdmin();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createTable(String tableName, String... columnFamilies) throws IOException {
        TableName tablename = TableName.valueOf(tableName);
        if (admin.tableExists(tablename)) {
            System.out.println("Table Exists");
        } else {
            System.out.println("Start create table");
            HTableDescriptor tableDescriptor = new HTableDescriptor(tablename);
            for (String columnFamily : columnFamilies) {
                HTableDescriptor column = tableDescriptor.addFamily(new HColumnDescriptor(columnFamily));
            }
            admin.createTable(tableDescriptor);
            System.out.println("Create Table success");
        }
    }

    /**
     * 获取一列获取一行数据
     *
     * @param tableName
     * @param rowKey
     * @param familyName
     * @param column
     * @return
     * @throws IOException
     */
    public static String getData(String tableName, String rowKey, String familyName, String column) throws IOException {
        Table table = conn.getTable(TableName.valueOf(tableName));
        byte[] row = Bytes.toBytes(rowKey);
        Get get = new Get(row);
        Result result = table.get(get);
        byte[] resultValue = result.getValue(familyName.getBytes(), column.getBytes());
        if (null == resultValue) {
            return null;
        }
        return new String(resultValue);
    }


    /**
     * 获取一行的所有数据 并且排序
     *
     * @param tableName 表名
     * @param rowKey    列名
     * @throws IOException
     */
    public static List<Map.Entry<String, Double>> getRow(String tableName, String rowKey) throws IOException {
        Table table = conn.getTable(TableName.valueOf(tableName));
        byte[] row = Bytes.toBytes(rowKey);
        Get get = new Get(row);
        Result r = table.get(get);

        HashMap<String, Double> rst = new HashMap<>();

        for (Cell cell : r.listCells()) {
            String key = Bytes.toString(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());
            String value = Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
            rst.put(key, new Double(value));
        }

        List<Map.Entry<String, Double>> ans = new ArrayList<>();
        ans.addAll(rst.entrySet());

        ans.sort((m1, m2) -> new Double(m1.getValue() - m2.getValue()).intValue());

        return ans;
    }

    /**
     * 向对应列添加数据
     *
     * @param tablename  表名
     * @param rowkey     行号
     * @param famliyname 列族名
     * @param column     列名
     * @param data       数据
     * @throws Exception
     */
    public static void putData(String tablename, String rowkey, String famliyname, String column, String data) throws Exception {
        Table table = conn.getTable(TableName.valueOf(tablename));
        Put put = new Put(rowkey.getBytes());
        put.addColumn(famliyname.getBytes(), column.getBytes(), data.getBytes());
        table.put(put);
    }

    /**
     * 将该单元格加1
     *
     * @param tablename  表名
     * @param rowkey     行号
     * @param familyname 列族名
     * @param column     列名
     * @throws Exception
     */
    public static void increamColumn(String tablename, String rowkey, String familyname, String column) throws Exception {
        String val = getData(tablename, rowkey, familyname, column);
        int res = 1;
        if (val != null) {
            res = Integer.parseInt(val) + 1;
        }
        putData(tablename, rowkey, familyname, column, String.valueOf(res));
    }

    public static void main(String[] args) throws IOException {
        List<Map.Entry<String, Double>> ps = HbaseClient.getRow("ps", "1");
        ps.forEach(System.out::println);
    }


    /**
     * 取出表中所有的key
     *
     * @param tableName
     * @return
     */
    public static List<String> getAllKey(String tableName) throws IOException {
        List<String> keys = new ArrayList<>();
        Scan scan = new Scan();
        Table table = HbaseClient.conn.getTable(TableName.valueOf(tableName));
        ResultScanner scanner = table.getScanner(scan);
        for (Result r : scanner) {
            keys.add(new String(r.getRow()));
        }
        return keys;
    }

}