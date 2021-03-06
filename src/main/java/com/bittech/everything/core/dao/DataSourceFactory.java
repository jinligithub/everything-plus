package com.bittech.everything.core.dao;

import com.alibaba.druid.pool.DruidDataSource;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Author: secondriver
 * Created: 2019/2/14
 * Description: 比特科技，只为更好的你；你只管学习，其它交给我。
 */
public class DataSourceFactory {
    
    /**
     * 数据源（单例）
     */
    private static volatile DruidDataSource dataSource;
    
    private DataSourceFactory() {
    
    }
    
    public static DataSource dataSource() {
        if (dataSource == null) {
            synchronized(DataSourceFactory.class) {
                if (dataSource == null) {
                    //实例化
                    dataSource = new DruidDataSource();
                    //JDBC  driver class
                    dataSource.setDriverClassName("org.h2.Driver");
                    //url, username, password
                    //采用的是H2的嵌入式数据库，数据库以本地文件的方式存储，只需要提供url接口
                    //JDBC规范中关于MySQL jdbc:mysql://ip:port/databaseName
                    
                    //获取当前工程路径
                    String workDir = System.getProperty("user.dir");
                    
                    //JDBC规范中关于H2 jdbc:h2:filepath ->存储到本地文件
                    //JDBC规范中关于H2 jdbc:h2:~/filepath ->存储到当前用户的home目录
                    //JDBC规范中关于H2 jdbc:h2://ip:port/databaseName ->存储到服务器
                    dataSource.setUrl("jdbc:h2:" + workDir + File.separator + "everything_plus");
                }
            }
        }
        return dataSource;
    }
    
    public static void initDatabase() {
        //1.获取数据源
        DataSource dataSource = DataSourceFactory.dataSource();
        //2.获取SQL语句
        //不采取读取绝对路径文件
//        E:\worskpace\java4\everything-plus\src\main\resources\everything_plus.sql
        //采取读取classpath路径下的文件
        //try-with-resources
        try (InputStream in = DataSourceFactory.class.getClassLoader().getResourceAsStream("everything_plus.sql");) {
            if (in == null) {
                throw new RuntimeException("Not read init database script please check it");
            }
            StringBuilder sqlBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in));) {
                String line = null;
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("--")) {
                        sqlBuilder.append(line);
                    }
                }
            }
            //3.获取数据库连接和名称执行SQL
            String sql = sqlBuilder.toString();
            //JDBC
            //3.1获取数据库的连接
            Connection connection = dataSource.getConnection();
            //3.2创建命令
            PreparedStatement statement = connection.prepareStatement(sql);
            //3.3执行SQL语句
            statement.execute();
            connection.close();
            statement.close();
        } catch (IOException e) {
        
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
