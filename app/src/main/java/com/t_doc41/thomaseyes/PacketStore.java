package com.t_doc41.thomaseyes;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class PacketStore
{
    private List<String> packets;
    private int maxPacketSize;

    public PacketStore(final int maxPacketSize)
    {
        this.maxPacketSize = maxPacketSize;
        this.packets = new ArrayList<>();
    }

    public String takePacket()
    {
        synchronized (packets)
        {
            String ret = null;
            if (packets.size() > 0)
            {
                ret = packets.remove(0);
            }
            return ret;
        }
    }

    public void submitData(final String data)
    {
        synchronized (packets)
        {
            if (packets.size() > 0)
            {
                String mostRecentPacket = packets.get(packets.size() - 1);
                if (data.length() + mostRecentPacket.length() <= maxPacketSize)
                {
                    mostRecentPacket = mostRecentPacket.concat(",").concat(data);
                    packets.set(packets.size() - 1, mostRecentPacket);
                }
                else
                {
                    packets.add(data);
                }
            }
            else
            {
                packets.add(data);
            }
        }
    }

    private void log(final String str)
    {
        Log.d(PacketStore.class.getSimpleName().toUpperCase(), str);
    }
}
