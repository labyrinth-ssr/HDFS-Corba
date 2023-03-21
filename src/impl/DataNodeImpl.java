package impl;
//TODO: your implementation
import api.DataNodePOA;

import java.util.ArrayList;

public class DataNodeImpl extends DataNodePOA {

    class Block{
        byte[] data;
    }
    ArrayList<Block> blocks = new ArrayList<>();


    int size = 0;

    public DataNodeImpl(){
        Block newBlock = new Block();
        newBlock.data = new byte [0];
        blocks.add(newBlock);
    }


    // 每个node
    @Override
    public byte[] read(int block_id) {
        return blocks.get(block_id).data;
    }

    @Override
    public void append(int block_id, byte[] bytes) {
        Block block = blocks.get(block_id);
        byte[] byte_3 = new byte[block.data.length+bytes.length];
        System.arraycopy(block.data, 0, byte_3, 0, block.data.length);
        System.arraycopy(bytes, 0, byte_3, block.data.length, bytes.length);
        block.data = byte_3;
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
