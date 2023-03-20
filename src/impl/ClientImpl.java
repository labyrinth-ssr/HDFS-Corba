package impl;
//TODO: your implementation
import api.*;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Scanner;

public class ClientImpl implements Client {

    static NameNode remoteObj;

    @Override
    public int open(String filepath, int mode) {

        return 0;
    }

    @Override
    public void append(int fd, byte[] bytes) {

    }

    @Override
    public byte[] read(int fd) {
        return new byte[0];
    }

    @Override
    public void close(int fd) {

    }


    public static void printMenu() {
        System.out.println("*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("*\t\t\t\t\t\t\t\t�ֲ�ʽ�ļ�ϵͳ\t\t\t\t\t\t\t\t\t\t\t\t");
        System.out.println("*\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t");
        System.out.println("*\t\topen\t\tread\t\tappend\t\tclose\t\texit");
        System.out.println("*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
    }

    public static int parse_command(Scanner input){
        String command = "";
        if (input.hasNext()){
            command = input.next();
        }
        if (Objects.equals(command, "open"))
            return 1;
        else if (Objects.equals(command, "read")) {
            return 2;
        } else if (Objects.equals(command, "append")) {
            return 3;
        } else if (Objects.equals(command, "close")) {
            return 4;
        } else if (Objects.equals(command, "exit")) {
            return 5;
        }
        return 0;

    }

    public static int parse_mode(String mode){
        int res = 0;

        if (Objects.equals(mode, "r"))
            return 1;
        else if (Objects.equals(mode, "w")) {
            return 2;
        } else if (Objects.equals(mode, "rw")) {
            return 3;
        }
        return 0;
    }

    public static void main(String[] args) {
        try {
            Properties properties = new Properties();
            properties.put("org.omg.CORBA.ORBInitialHost", "127.0.0.1");  //ָ��ORB��ip��ַ
            properties.put("org.omg.CORBA.ORBInitialPort", "1050");       //ָ��ORB�Ķ˿�
            // �½�ORB����
            ORB orb = ORB.init(args, properties);

            // Naming ������
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            // ��Namingע������л�ȡ��Զ�̶���
            String name = "FileOps";
            remoteObj = NameNodeHelper.narrow(ncRef.resolve_str(name));

            System.out.println("Obtained a handle on server object");

            Scanner input = new Scanner(System.in);
            boolean m = true;   //����whileѭ��
            int n;              //switch�ж�

            while (m) {
                printMenu();
                System.out.println(">>");
                n = parse_command(input);
                // ���˵�nextInt()����Ŀ��ַ� ref��https://blog.csdn.net/wjy1090233191/article/details/42080029
                input.nextLine();
                switch (n) {
                    case 1: // open
                        String fileName = input.next();
                        int mode = parse_mode(input.next());
                        String fileDesc = remoteObj.open(fileName,mode);
//                        System.out.println("INFO: fd="+fd);
                        break;
//                    case 2:
//                        // resultList��һ�����ز���
//                        MyListHolder resultList = new MyListHolder();
//                        remoteObj.listAllFile(resultList);
//                        for (int i = 0; i < resultList.value.length; i++) {
//                            System.out.println(resultList.value[i]);
//                        }
//                        break;
//                    case 3:
//                        System.out.println("������Ҫ���ص��ļ�����������׺����:");
//                        String dFileName = input.nextLine();
//                        // result��һ�����ز���
//                        MyDataHolder result = new MyDataHolder();
//                        boolean downloadFlag = remoteObj.download("serverFile/" +dFileName, result);
//                        if (downloadFlag) {
//                            // ���浽�ͻ����ļ�ϵͳ
//                            FileOutputStream fos = new FileOutputStream("clientFile/" + dFileName);
//                            fos.write(result.value);
//                            fos.flush();
//                            fos.close();
//                            System.out.println("���سɹ�!");
//                        } else {
//                            System.out.println("����ʧ��!����������ļ��ڷ��������Ƿ����");
//                        }
//                        break;
//                    case 4:
//                        System.out.println("������Ҫ�ϴ����ļ�����������׺���������ϴ������ļ��У�������ѹ���ļ�:");
//                        String uFileName = input.nextLine();
//
//                        MyDataHolder in = new MyDataHolder();
//                        try {
//                            // �ӿͻ����ļ��õ��ֽ���
//                            FileInputStream fis = new FileInputStream("clientFile/" + uFileName);
//                            byte[] data = new byte[fis.available()];
//                            fis.read(data);
//                            fis.close();
//                            in.value = data;
//                            remoteObj.upload(uFileName, in.value);
//                            System.out.println("�ϴ��ɹ�!");
//                            break;
//                        } catch (Exception e) {
//                            System.out.println("�ϴ�ʧ��!����������ļ��ڷ��������Ƿ����");
//                            break;
//                        }

                    case 5:
                        m = false;
                        break;
                    default:
                        System.out.println("������������ԣ�");

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
