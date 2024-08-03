package com.ufs.dcomp;

import java.util.Scanner;
import com.rabbitmq.client.*;
import java.io.IOException;

public class Main {
    private static String target = "";
    private static String currentUser = "";
    private static String message;

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("34.228.100.244"); // Alterar
        factory.setUsername("admin"); // Alterar
        factory.setPassword("PedroW2001"); // Alterar
        factory.setVirtualHost("/");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        Scanner sc = new Scanner(System.in);

        System.out.print("User: ");
        currentUser = sc.nextLine();

        channel.queueDeclare(currentUser, false, false, false, null);
        safePrintln("\nLogado com sucesso!");

        System.out.print("Destinatário: ");
        target = sc.nextLine();

        while (true) {
            Consumer consumer = new DefaultConsumer(channel) {
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                        byte[] body) throws IOException {
                    String messageReceived = new String(body, "UTF-8");
                    safePrintln("\n[x] Mensagem recebida: " + messageReceived);
                    safePrint(target + " >> ");
                }
            };

            channel.basicConsume(currentUser, true, consumer);

            safePrint(target + " >> ");
            message = sc.nextLine();

            if (message.isEmpty()) {
                continue;
            }

            if (message.toLowerCase().equals("sair")) {
                break;
            }

            if (message.contains("@")) {
                target = message.substring(message.lastIndexOf("@") + 1);
                safePrint("");
                continue;
            }

            channel.basicPublish("", target, null, message.getBytes("UTF-8"));
        }

        sc.close();
        channel.close();
        connection.close();
    }

    private static void safePrintln(String s) {
        synchronized (System.out) {
            System.out.println(s);
        }
    }

    private static void safePrint(String s) {
        synchronized (System.out) {
            System.out.print(s);
        }
    }
}
