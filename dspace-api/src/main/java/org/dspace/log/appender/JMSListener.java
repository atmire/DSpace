//package org.dspace.log.appender;
//
//import org.apache.log4j.Logger;
//import org.springframework.jms.annotation.JmsListener;
//import org.springframework.stereotype.Component;
//
//
//@Component
//public class JMSListener{
//    private final Logger log = Logger.getLogger(JMSListener.class);
//
//    @JmsListener(destination = "anotherTest", containerFactory = "myFactory")
//    public void processMessage(String content) {
//        log.error("Hier");
//        log.info("Hier");
//        System.out.println("hier");
//        String t = content;
//        throw new IllegalArgumentException();
//    }
//
////    @JmsListener(destination = "ActiveMQ.Advisory.Consumer.Queue.anotherTest", containerFactory = "myFactory")
////    public void test(String test){
////        log.error("test");
////        log.info("test");
////        System.out.println("test");
////        String t = test;
////        throw new IllegalArgumentException();
////    }
//}
