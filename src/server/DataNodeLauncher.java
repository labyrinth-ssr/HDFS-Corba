package server;

import api.DataNodeHelper;
import impl.DataNodeImpl;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import api.DataNode;

import java.util.Properties;

public class DataNodeLauncher {

    public static void main(String[] args) {
        try {
            Properties properties = new Properties();
            properties.put("org.omg.CORBA.ORBInitialHost", "127.0.0.1");  //指定ORB的ip地址
//            properties.put("org.omg.CORBA.ORBInitialPort", "1051");       //指定ORB的端口

            // 初始化ORB对象
            ORB orb = ORB.init(args, properties);

            // 获取到rootPOA的引用，激活POAManager
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();

            // 创建一个接口实现对象
            DataNodeImpl fileServant = new DataNodeImpl();

            // 将服务实现对象交给POA管理，并向外暴露接口
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(fileServant);
            DataNode href = DataNodeHelper.narrow(ref);

            // Naming 上下文
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            // 绑定远程对象到Naming
            String name = "DataNode";
            NameComponent[] path = ncRef.to_name(name);
            ncRef.rebind(path, href);

//            fileServant.initFileDescs();

            System.out.println("FileServer ready and waiting...");

            // 等待客户端调用
            orb.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("FileServer Exiting...");
    }
}



