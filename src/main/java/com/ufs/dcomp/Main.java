package com.ufs.dcomp;

import com.rabbitmq.client.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import org.json.JSONObject;

public class Main {
    private static String target = "";
    private static String currentUser = "";
    private static String message;

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("peredo");
        factory.setPassword("peredo");
        factory.setVirtualHost("/");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        Scanner sc = new Scanner(System.in, StandardCharsets.UTF_8.name());

        System.out.print("User: ");
        currentUser = sc.nextLine();

        while (currentUser.isEmpty()) {
            System.out.print("\033[H\033[2J");
            System.out.println("Usuário inválido. Por favor, tente novamente.");
            System.out.print("Usuário: ");
            currentUser = sc.nextLine();
        }

        // Declara a fila para o usuário atual
        channel.queueDeclare(currentUser, false, false, false, null);
        safePrintln("\nLogado com sucesso!");

        safePrint(">> ");

        // Define o consumidor para ouvir mensagens
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                    byte[] body) throws IOException {
                String jsonMessage = new String(body, StandardCharsets.UTF_8);
                JSONObject jsonObject = new JSONObject(jsonMessage);

                String sender = jsonObject.getString("sender");
                String timestamp = jsonObject.getString("timestamp");
                String msgContent = jsonObject.getString("message");

                String formattedMessage = String.format("(%s) %s diz: %s", timestamp, sender, msgContent);
                safePrintln("\n" + formattedMessage);
                safePrint(target + ">> ");
            }
        };

        channel.basicConsume(currentUser, true, consumer);

        while (true) {
            // Recebe a mensagem do usuário
            message = sc.nextLine();

            if (message.toLowerCase().equals("sair")) {
                break;
            }

            if (message.startsWith("@")) {
                target = message.substring(1).trim();
                safePrint(target + ">> ");
                continue;
            }

            if (target.isEmpty()) {
                safePrintln("Por favor, defina um destinatário usando @nome.");
                safePrint(">> ");
                continue;
            }

            if (message.isEmpty() || target.isEmpty()) {
                safePrint(target + ">> ");
                continue;
            }

            String formattedMessage = String.format(
                    "{\"sender\":\"%s\", \"timestamp\":\"%s\", \"message\":\"%s\"}",
                    currentUser,
                    new SimpleDateFormat("dd/MM/yyyy 'às' HH:mm:ss").format(new Date()),
                    message);

            channel.basicPublish("", target, null, formattedMessage.getBytes(StandardCharsets.UTF_8));

            safePrint(target + ">> ");
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
