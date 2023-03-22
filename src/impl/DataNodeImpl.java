package impl;
//TODO: your implementation
import api.DataNodePOA;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public class DataNodeImpl extends DataNodePOA {

    class Block{
        int id;
        byte[] data = new byte[0];
    }
    ArrayList<Block> blocks = new ArrayList<>();
    int size = 0;

    public DataNodeImpl(){
//        Block newBlock = new Block();
//        newBlock.data = new byte [0];
//        blocks.add(newBlock);
    }


    // 每个node
    @Override
    public byte[] read(int block_id) {
        System.out.println("read");
        return blocks.get(block_id).data;
    }

    private Block getBlockById(int id) {
        for (Block block : blocks) {
            if (block.id == id) {
                return block;
            }
        }
        return null;
    }

    @Override
    public void append(int block_id, byte[] bytes) {
        System.out.println("write");
        Block block;
        if ((block = getBlockById(block_id))==null){
            block = new Block();
            block.id = block_id;
            blocks.add(block);
        }
        byte[] byte_3 = new byte[block.data.length+bytes.length];
        System.arraycopy(block.data, 0, byte_3, 0, block.data.length);
        System.arraycopy(bytes, 0, byte_3, block.data.length, bytes.length);
        block.data = byte_3;

        try {
            FileOutputStream fos = new FileOutputStream("block"+block_id+".txt");
            fos.write(byte_3);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int randomBlockId() {
        int min = 0;
        int max = blocks.size()-1;
        int res = min + (int)(Math.random()*(max - min +1));
        System.out.println("res"+res);
        return res;
    }
}
