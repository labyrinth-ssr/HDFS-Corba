package impl;
//TODO: your implementation
import api.*;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import utils.FileDesc;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ClientImpl implements Client {

    static NameNode namenode;
    static DataNode dataNode;
    int globalBlockId = 0;

    ArrayList<Integer> globalBlockIds = new ArrayList<>();

    class CFile{
        FileDesc fileDesc;
        int fd;
        ArrayList<Integer> blockIds;
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


    // 假设client不会重复打开一个文件
    // 必然新建一个CFile在内存中
    @Override
    public int open(String filepath, int mode) {
        FileDesc fileDesc = FileDesc.fromString(namenode.open(filepath,mode));
        CFile newFile = new CFile();
        newFile.fileDesc=fileDesc;
        newFile.fd = files.size();
        newFile.blockIds = new ArrayList<>();
        System.out.println("client: get from namenode:"+fileDesc.toString());
        files.add(newFile);
        return newFile.fd;
    }


    // 创建需要先打开，空文件没有blockid
    @Override
    public void append(int fd, byte[] bytes) {
        CFile file = getFileByFd(fd);
        FileDesc fileDesc = file.fileDesc;
        int blockId = globalBlockId++;
        System.out.println("block id:"+blockId);
        if (fileDesc.getMode() == 0b10 || fileDesc.getMode() == 0b11){
            dataNode.append(blockId,bytes);
        } else {
            System.out.println("client: append: no write auth");
        }
    }

    @Override
    public byte[] read(int fd) {
        CFile file = getFileByFd(fd);
        FileDesc fileDesc = file.fileDesc;
        System.out.println("client: read: mode:"+fileDesc.getMode());
        if (fileDesc.getMode() == 0b01 || fileDesc.getMode() == 0b11){
            if (fileDesc.getLocations().size()==0){
                return "".getBytes(StandardCharsets.UTF_8);
            }
            int blockId = fileDesc.getLocations().get(0);
            System.out.println("block id:"+blockId);
            return dataNode.read(blockId);
        }
        else return null;
    }

    @Override
    public void close(int fd) {
        CFile file = getFileByFd(fd);
        files.remove(file);
        namenode.close(file.fileDesc.toString());
    }

    static int fd;

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

    public void run(String[] args) {
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
            namenode = NameNodeHelper.narrow(ncRef.resolve_str(name));

            Properties properties2 = new Properties();
            properties2.put("org.omg.CORBA.ORBInitialHost", "127.0.0.1");  //指定ORB的ip地址
            properties2.put("org.omg.CORBA.ORBInitialPort", "1051");       //指定ORB的端口
            // 新建ORB对象
            ORB orb2 = ORB.init(args, properties2);

            // Naming 上下文
            org.omg.CORBA.Object objRef2 = orb2.resolve_initial_references("NameService");
            NamingContextExt ncRef2 = NamingContextExtHelper.narrow(objRef2);

            // 从Naming注册服务中获取到远程对象
            String name2 = "DataNode";
            dataNode = DataNodeHelper.narrow(ncRef2.resolve_str(name2));

            System.out.println("Obtained a handle on server object");

            Scanner input = new Scanner(System.in);
            boolean m = true;   //用于while循环
            int n;              //switch判断

            while (m) {
                printMenu();
                System.out.print(">>");
                n = parse_command(input);
                // 过滤掉nextInt()后面的空字符 ref：https://blog.csdn.net/wjy1090233191/article/details/42080029
                switch (n) {
                    case 1: // open
                        String fileName = input.next();
                        int mode = parse_mode(input.next());
                        input.nextLine();
                        fd = this.open(fileName, mode);
                        System.out.println("INFO: fd="+fd);
                        break;
                    case 2:
                        fd = input.nextInt();
                        input.nextLine();
                        System.out.println("read "+fd);
                        byte[] res = this.read(fd);
                        if (res==null){
                            System.out.println("INFO: READ not allowed");
                        } else {
                            String resstr = new String(res);
                            System.out.println(resstr);
                        }
                        break;
//                    append 1 hello world
                    case 3:
                        fd = input.nextInt();
                        String content = input.nextLine();
                        System.out.println("write:"+content);
                        this.append(fd,content.getBytes(StandardCharsets.UTF_8));
                        break;
                    case 4:
                        input.nextLine();
                        this.close(fd);
                        break;
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
