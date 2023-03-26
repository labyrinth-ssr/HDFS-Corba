package impl;
//TODO: your implementation
import api.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import utils.FileDesc;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ClientImpl implements Client {

    NameNode namenode;
    ArrayList<DataNode> dataNode = new ArrayList<>();

//    for Unit test.
//    NameNodeImpl namenode = new NameNodeImpl();
//    ArrayList<DataNodeImpl> dataNode = new ArrayList<DataNodeImpl>(Arrays.asList(new DataNodeImpl(), new DataNodeImpl()));
    int globalBlockId = 0;
    int lastWriteByte = 0;

    ArrayList<Integer> globalBlockIds = new ArrayList<>();

    class CFile{
        FileDesc fileDesc;
        int fd;
//        ArrayList<Integer> blockIds;
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
//        newFile.blockIds = new ArrayList<>();
//        System.out.println("client: get from namenode:"+fileDesc.toString());
        files.add(newFile);
        return newFile.fd;
    }
    public ArrayList<Integer> jsonToList(String json) {

        Gson gson = new Gson();
        ArrayList<Integer> persons = gson.fromJson(json, new TypeToken<ArrayList<Integer>>() {
        }.getType());//对于不是类的情况，用这个参数给出

        return persons;
    }

    // 创建需要先打开，空文件没有blockid
    @Override
    public void append(int fd, byte[] bytes) {
        CFile file = getFileByFd(fd);
        FileDesc fileDesc = file.fileDesc;
        int size= bytes.length/(4*1024)+1;
//        int size = bytes.length/(4*1024)+1;
        String locationstr = namenode.getLocations(fileDesc.getId(),size);
        ArrayList<Integer> locations = jsonToList(locationstr);
        System.out.println("size"+size);
        for (int i=0;i<size;i++){
            int blockId;
            if (lastWriteByte < 4*1024 && !fileDesc.getBlockIds().isEmpty()){
                blockId = globalBlockId;
                lastWriteByte +=bytes.length;
            } else {
                blockId = globalBlockId++;
                lastWriteByte = bytes.length;
                fileDesc.addBlockIds(blockId);
            }
            System.out.println("client:append:block id:"+blockId);
            if (fileDesc.getMode() == 0b10 || fileDesc.getMode() == 0b11){
                dataNode.get(locations.get(i)).append(blockId,bytes);
            } else {
                System.out.println("client: append: no write auth");
            }
        }
//        namenode.setFileInfos(fileDesc.getId(),size,locations);
        fileDesc.setLocations(locations);
        fileDesc.setFileSize(size);
    }

    @Override
    public byte[] read(int fd) {
        StringBuilder sb = new StringBuilder();
        CFile file = getFileByFd(fd);
        FileDesc fileDesc = file.fileDesc;
        System.out.println("client: read: mode:"+fileDesc.getMode());
        byte[] res = new byte[0];
        if (fileDesc.getMode() == 0b01 || fileDesc.getMode() == 0b11){
            if (fileDesc.getLocations().size()==0){
                System.out.println("no location");
                return "".getBytes(StandardCharsets.UTF_8);
            }
            for (int i=0;i<fileDesc.getFileSize();i++){
                int blockId = fileDesc.getBlockIds().get(i);
                System.out.println("client read:block id:"+blockId);

                byte[] array2 = dataNode.get(fileDesc.getLocations().get(i)).read(blockId);
                byte[] result = new byte[res.length + array2.length];
                System.arraycopy(res, 0, result, 0, res.length);
                System.arraycopy(array2, 0, result, res.length, array2.length);

                res = result;
//                sb.append(Arrays.toString(dataNode.get(fileDesc.getLocations().get(i)).read(blockId)));
            }
            return res;
        }
        else return null;
    }

    @Override
    public void close(int fd) {
        CFile file = getFileByFd(fd);
        files.remove(file);
        namenode.close(file.fileDesc.toString());
    }

    public void exit(){
        for (CFile file : files) {
            close(file.fd);
        }
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
            DataNode dn1 = DataNodeHelper.narrow(ncRef2.resolve_str(name2));
            dataNode.add(dn1);

            Properties properties3 = new Properties();
            properties3.put("org.omg.CORBA.ORBInitialHost", "127.0.0.1");  //指定ORB的ip地址
            properties3.put("org.omg.CORBA.ORBInitialPort", "1052");       //指定ORB的端口
            // 新建ORB对象
            ORB orb3 = ORB.init(args, properties3);
            // Naming 上下文
            org.omg.CORBA.Object objRef3 = orb3.resolve_initial_references("NameService");
            NamingContextExt ncRef3 = NamingContextExtHelper.narrow(objRef3);
            // 从Naming注册服务中获取到远程对象
            String name3 = "DataNode";
            dataNode.add(DataNodeHelper.narrow(ncRef3.resolve_str(name2)));

            Scanner input = new Scanner(System.in);
            boolean m = true;   //用于while循环
            int n;              //switch判断

            while (m) {
                printMenu();
                System.out.print(">>");
                n = parse_command(input);
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
                        String content = input.nextLine().trim();
                        this.append(fd,content.getBytes(StandardCharsets.UTF_8));
                        System.out.println("INFO: write done");
                        break;
                    case 4:
                        input.nextLine();
                        this.close(fd);
                        System.out.println("INFO: fd "+fd+" closed");
                        break;
                    case 5:
                        m = false;
                        this.exit();
                        System.out.println("INFO: bye");
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
