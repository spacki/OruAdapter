package com.ge.hc.oru;

import org.apache.camel.spring.Main;

public class OruServer {

    public static void main(String[] args) throws Exception {

        System.out.println("Encoding: " + System.getProperty("file.encoding"));
        System.out.println("Charset: " + java.nio.charset.Charset.defaultCharset());
        Main.main("-ac", "/context.xml");
    }
    
}
