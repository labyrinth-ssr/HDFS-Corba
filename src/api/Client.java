package api;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

public interface Client {
    int open(String filepath, int mode);
    void append(int fd, byte[] bytes);

    /**
     * @param fd
     * @return All bytes in file, or null if the read is not allowed (file opened without r).
     */
    byte[] read(int fd);
    void close(int fd);


}
