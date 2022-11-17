package com.licheedev.serialtool.comn;

import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.licheedev.hwutils.ByteUtil;
import com.licheedev.myutils.LogPlus;
import com.licheedev.serialtool.comn.message.LogManager;
import com.licheedev.serialtool.comn.message.RecvMessage;
import com.licheedev.serialtool.util.ToastUtil;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 读串口线程
 */
public class SerialReadThread extends Thread {

    private static final String TAG = "SerialReadThread";

    private BufferedInputStream mInputStream;

    public SerialReadThread(InputStream is) {
        mInputStream = new BufferedInputStream(is);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void run() {
       // byte[] received = new byte[1024];
        byte[] received = new byte[2048];
        int size;

        while (true) {

            if (Thread.currentThread().isInterrupted()) {
                break;
            }
            try {

                int available = mInputStream.available();

                if (available > 0) {
                    size = mInputStream.read(received);
                    if (size > 0) {
                        onDataReceive(received, size);
                    }
                } else {
                    SystemClock.sleep(1);
                }
            } catch (IOException e) {
                LogPlus.e("读取数据失败", e);
            }
            //Thread.yield();
        }

        LogPlus.e("结束读进程");
    }

    /**
     * 处理获取到的数据
     *
     * @param received
     * @param size
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void onDataReceive(byte[] received, int size) {
        // TODO: 2018/3/22 解决粘包、分包等
        String hexStr = ByteUtil.bytes2HexStr(received, 0, size);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ToastUtil.recordLog1("================================*********Size("+size+")***********======================","l1_ComPort_hex_data");
            ToastUtil.recordLog1(hexStr,"l1_ComPort_hex_data");
        }


        String hexStr2 = new String(received, StandardCharsets.UTF_8);


        Log.d("OnNmea:-","Size:-"+size);
        Log.d("OnNmea:-","Message:-"+hexStr2);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            ToastUtil.recordLog2("================================*********Size("+size+")***********======================","l1_ComPort_string_data");

            ToastUtil.recordLog2(hexStr2,"l1_ComPort_string_data");
        }

        LogManager.instance().post(new RecvMessage(hexStr2,size));

       /* byte[] test= {  57, 46, 57, 57, 44, 44, 44, 44, 44, 44};
        String test2 = new String(test, StandardCharsets.UTF_8);
*/

    }

    /**
     * 停止读线程
     */
    public void close() {

        try {
            mInputStream.close();
        } catch (IOException e) {
            LogPlus.e("异常", e);
        } finally {
            super.interrupt();
        }
    }
}
