package impl;
//TODO: your implementation
import api.*;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import utils.FileDesc;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;

public class ClientImpl implements Client {

    static NameNode remoteObj;

    static NameNodeImpl nn = new NameNodeImpl();
    static DataNodeImpl dataNode = new DataNodeImpl();

    class CFile{
        FileDesc fileDesc;
        int fd;
    }

    ArrayList<CFile> files = new ArrayList<>();

    CFile getFileByFd(int fd){
        for (CFile file :
                files) {
            if (file.fd == fd)
                return file;
        }
        return null;
    }

    @Override
    public int open(String filepath, int mode) {
        FileDesc fileDesc = FileDesc.fromString(nn.open(filepath,mode));
        CFile newFile = new CFile();
        newFile.fileDesc=fileDesc;
        newFile.fd = files.size();
        files.add(newFile);

        return newFile.fd;
    }

    @Override
    public void append(int fd, byte[] bytes) {
        CFile file = getFileByFd(fd);
        FileDesc fileDesc = file.fileDesc;
        int blockId = fileDesc.getLocations().get(0);
        System.out.println("block id:"+blockId);
        if (fileDesc.getMode() == 0b10 || fileDesc.getMode() == 0b11){
            dataNode.append(blockId,bytes);
        }
    }

    @Override
    public byte[] read(int fd) {
        CFile file = getFileByFd(fd);
        FileDesc fileDesc = file.fileDesc;
        int blockId = fileDesc.getLocations().get(0);
        System.out.println("block id:"+blockId);
        if (fileDesc.getMode() == 0b01 || fileDesc.getMode() == 0b11)
            return dataNode.read(blockId);
        else return null;
    }

    @Override
    public void close(int fd) {
        CFile file = getFileByFd(fd);
        files.remove(file);
        nn.close(file.fileDesc.toString());
    }


    public static void printMenu() {
        System.out.println("*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("*\t\t\t\t\t\t\t\t分布式文件系统\t\t\t\t\t\t\t\t\t\t\t\t");
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
            properties.put("org.omg.CORBA.ORBInitialHost", "127.0.0.1");  //指定ORB的ip地址
            properties.put("org.omg.CORBA.ORBInitialPort", "1050");       //指定ORB的端口
            // 新建ORB对象
            ORB orb = ORB.init(args, properties);

            // Naming 上下文
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            // 从Naming注册服务中获取到远程对象
            String name = "FileOps";
            remoteObj = NameNodeHelper.narrow(ncRef.resolve_str(name));

            System.out.println("Obtained a handle on server object");

            Scanner input = new Scanner(System.in);
            boolean m = true;   //用于while循环
            int n;              //switch判断

            while (m) {
                printMenu();
                System.out.println(">>");
                n = parse_command(input);
                // 过滤掉nextInt()后面的空字符 ref：https://blog.csdn.net/wjy1090233191/article/details/42080029
                input.nextLine();
                switch (n) {
                    case 1: // open
                        String fileName = input.next();
                        int mode = parse_mode(input.next());
                        String fileDesc = remoteObj.open(fileName,mode);
//                        System.out.println("INFO: fd="+fd);
                        break;
//                    case 2:
//                        // resultList是一个返回参数
//                        MyListHolder resultList = new MyListHolder();
//                        remoteObj.listAllFile(resultList);
//                        for (int i = 0; i < resultList.value.length; i++) {
//                            System.out.println(resultList.value[i]);
//                        }
//                        break;
//                    case 3:
//                        System.out.println("输入想要下载的文件名（包括后缀名）:");
//                        String dFileName = input.nextLine();
//                        // result是一个返回参数
//                        MyDataHolder result = new MyDataHolder();
//                        boolean downloadFlag = remoteObj.download("serverFile/" +dFileName, result);
//                        if (downloadFlag) {
//                            // 保存到客户端文件系统
//                            FileOutputStream fos = new FileOutputStream("clientFile/" + dFileName);
//                            fos.write(result.value);
//                            fos.flush();
//                            fos.close();
//                            System.out.println("下载成功!");
//                        } else {
//                            System.out.println("下载失败!请检查输入的文件在服务器上是否存在");
//                        }
//                        break;
//                    case 4:
//                        System.out.println("输入想要上传的文件名（包括后缀名），若上传的是文件夹，请打包成压缩文件:");
//                        String uFileName = input.nextLine();
//
//                        MyDataHolder in = new MyDataHolder();
//                        try {
//                            // 从客户端文件得到字节流
//                            FileInputStream fis = new FileInputStream("clientFile/" + uFileName);
//                            byte[] data = new byte[fis.available()];
//                            fis.read(data);
//                            fis.close();
//                            in.value = data;
//                            remoteObj.upload(uFileName, in.value);
//                            System.out.println("上传成功!");
//                            break;
//                        } catch (Exception e) {
//                            System.out.println("上传失败!请检查输入的文件在服务器上是否存在");
//                            break;
//                        }

                    case 5:
                        m = false;
                        break;
                    default:
                        System.out.println("输入错误，请重试！");

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
