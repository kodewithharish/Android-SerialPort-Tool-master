package com.licheedev.serialtool.activity;

import android.content.Intent;
import android.os.Bundle;
import android.serialport.SerialPortFinder;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import ru.bartwell.exfilepicker.ExFilePicker;
import ru.bartwell.exfilepicker.data.ExFilePickerResult;

import com.licheedev.myutils.LogPlus;
import com.licheedev.serialtool.R;
import com.licheedev.serialtool.activity.base.BaseActivity;
import com.licheedev.serialtool.comn.Device;
import com.licheedev.serialtool.comn.SerialPortManager;
import com.licheedev.serialtool.model.Command;
import com.licheedev.serialtool.util.AllCapTransformationMethod;
import com.licheedev.serialtool.util.BaseListAdapter;
import com.licheedev.serialtool.util.CommandParser;
import com.licheedev.serialtool.util.ListViewHolder;
import com.licheedev.serialtool.util.PrefHelper;
import com.licheedev.serialtool.util.ToastUtil;
import com.licheedev.serialtool.util.constant.PreferenceKeys;

import static com.licheedev.serialtool.R.array.baudrates;

import java.io.File;
import java.util.List;

public class MainActivity extends BaseActivity implements AdapterView.OnItemSelectedListener,AdapterView.OnItemClickListener {

    @BindView(R.id.spinner_devices)
    Spinner mSpinnerDevices;
    @BindView(R.id.spinner_baudrate)
    Spinner mSpinnerBaudrate;
    @BindView(R.id.btn_open_device)
    Button mBtnOpenDevice;
    @BindView(R.id.btn_send_data)
    Button mBtnSendData;
    @BindView(R.id.btn_load_list)
    Button mBtnLoadList;
    @BindView(R.id.et_data)
    EditText mEtData;

    @BindView(R.id.list_view_)
    ListView getmListView;

    private Device mDevice;

    private int mDeviceIndex;
    private int mBaudrateIndex;

    private String[] mDevices;
    private String[] mBaudrates;

    private boolean mOpened = false;
    private ExFilePicker mFilePicker;
    private CommandParser mParser;
    private InnerAdapter mAdapter;
    public static final int REQUEST_FILE = 233;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mEtData.setTransformationMethod(new AllCapTransformationMethod(true));

        initDevice();
        initSpinners();
        updateViewState(mOpened);


        initFilePickers();
        mParser = new CommandParser();
        getmListView.setOnItemClickListener(this);
        mAdapter = new InnerAdapter();
        getmListView.setAdapter(mAdapter);

        parsetxtFile(new File(String.valueOf(PrefHelper.getDefault().getString( PreferenceKeys.CMD_FILE_PATH,"cmd"))));

    }

    @Override
    protected void onDestroy() {
        SerialPortManager.instance().close();
        super.onDestroy();
    }


    @Override
    protected boolean hasActionBar() {
        return false;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }


    private void initDevice() {

        SerialPortFinder serialPortFinder = new SerialPortFinder();

        mDevices = serialPortFinder.getAllDevicesPath();
        if (mDevices.length == 0) {
            mDevices = new String[]{
                    getString(R.string.no_serial_device)
            };
        }
        mBaudrates = getResources().getStringArray(baudrates);

        mDeviceIndex = PrefHelper.getDefault().getInt(PreferenceKeys.SERIAL_PORT_DEVICES, 0);
        mDeviceIndex = mDeviceIndex >= mDevices.length ? mDevices.length - 1 : mDeviceIndex;
        mBaudrateIndex = PrefHelper.getDefault().getInt(PreferenceKeys.BAUD_RATE, 0);

        mDevice = new Device(mDevices[mDeviceIndex], mBaudrates[mBaudrateIndex]);
    }


    private void initSpinners() {

        ArrayAdapter<String> deviceAdapter =
                new ArrayAdapter<String>(this, R.layout.spinner_default_item, mDevices);
        deviceAdapter.setDropDownViewResource(R.layout.spinner_item);
        mSpinnerDevices.setAdapter(deviceAdapter);
        mSpinnerDevices.setOnItemSelectedListener(this);

        ArrayAdapter<String> baudrateAdapter =
                new ArrayAdapter<String>(this, R.layout.spinner_default_item, mBaudrates);
        baudrateAdapter.setDropDownViewResource(R.layout.spinner_item);
        mSpinnerBaudrate.setAdapter(baudrateAdapter);
        mSpinnerBaudrate.setOnItemSelectedListener(this);

        mSpinnerDevices.setSelection(mDeviceIndex);
        mSpinnerBaudrate.setSelection(mBaudrateIndex);
    }

    @OnClick({R.id.btn_open_device, R.id.btn_send_data, R.id.btn_load_list})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_open_device:
                switchSerialPort();
                break;
            case R.id.btn_send_data:
                sendData();
                break;
            case R.id.btn_load_list:
               // startActivity(new Intent(this, LoadCmdListActivity.class));
                mFilePicker.start(this, REQUEST_FILE);
                break;
        }
    }

    private void sendData() {

        String text = mEtData.getText().toString().trim();
        /*if (TextUtils.isEmpty(text) || text.length() % 2 != 0) {
            ToastUtil.showOne(this, "无效数据");
            return;
        }*/

        SerialPortManager.instance().sendCommand(text);
    }

    /**
     */
    private void switchSerialPort() {
        if (mOpened) {
            SerialPortManager.instance().close();
            mOpened = false;
        } else {

            PrefHelper.getDefault().saveInt(PreferenceKeys.SERIAL_PORT_DEVICES, mDeviceIndex);
            PrefHelper.getDefault().saveInt(PreferenceKeys.BAUD_RATE, mBaudrateIndex);

            mOpened = SerialPortManager.instance().open(mDevice) != null;
            if (mOpened) {
                ToastUtil.showOne(this, "Serial Port Opend");
            } else {
                ToastUtil.showOne(this, "Failed to Open SerialPort");
            }
        }
        updateViewState(mOpened);
    }

    /**
     * 更新视图状态
     *
     * @param isSerialPortOpened
     */
    private void updateViewState(boolean isSerialPortOpened) {

        int stringRes = isSerialPortOpened ? R.string.close_serial_port : R.string.open_serial_port;

        mBtnOpenDevice.setText(stringRes);

        mSpinnerDevices.setEnabled(!isSerialPortOpened);
        mSpinnerBaudrate.setEnabled(!isSerialPortOpened);
        mBtnSendData.setEnabled(isSerialPortOpened);
        mBtnLoadList.setEnabled(isSerialPortOpened);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        // Spinner 选择监听
        switch (parent.getId()) {
            case R.id.spinner_devices:
                mDeviceIndex = position;
                mDevice.setPath(mDevices[mDeviceIndex]);
                break;
            case R.id.spinner_baudrate:
                mBaudrateIndex = position;
                mDevice.setBaudrate(mBaudrates[mBaudrateIndex]);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_FILE) {
            ExFilePickerResult result = ExFilePickerResult.getFromIntent(data);
            if (result != null && result.getCount() > 0) {
                File file = new File(result.getPath(), result.getNames().get(0));
                parsetxtFile(file);
            } else {
                ToastUtil.showOne(this, "no file selected");
            }
        }
    }

    private void parsetxtFile(File file)
    {

        PrefHelper.getDefault().saveString(PreferenceKeys.CMD_FILE_PATH, file.getPath().toString());

        mParser.rxParse(file).subscribe(new Observer<List<Command>>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(List<Command> commands) {
                mAdapter.setNewData(commands);
            }

            @Override
            public void onError(Throwable e) {
                LogPlus.e("parsing failed", e);
            }

            @Override
            public void onComplete() {

            }
        });

    }


    private void initFilePickers() {

        mFilePicker = new ExFilePicker();
        mFilePicker.setNewFolderButtonDisabled(true);
        mFilePicker.setQuitButtonEnabled(true);
        mFilePicker.setUseFirstItemAsUpEnabled(true);
        mFilePicker.setCanChooseOnlyOneItem(true);
        mFilePicker.setShowOnlyExtensions("txt");
        mFilePicker.setChoiceType(ExFilePicker.ChoiceType.FILES);
    }


   /* @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Command item = mAdapter.getItem(position);
        SerialPortManager.instance().sendCommand(item.getCommand());
    }*/

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Command item = mAdapter.getItem(position);
       // SerialPortManager.instance().sendCommand(item.getCommand());
        mEtData.setText(item.getCommand().trim());
        mEtData.setSelection(item.getCommand().trim().length());
    }

    private static class InnerAdapter extends BaseListAdapter<Command> {
        @Override
        protected void inflateItem(ListViewHolder holder, int position) {

            Command item = getItem(position);

            String comment = String.valueOf(position + 1);
            comment =
                    TextUtils.isEmpty(item.getComment()) ? comment : comment + " " + item.getComment();

            holder.setText(R.id.tv_comment, comment);
            holder.setText(R.id.tv_command, item.getCommand());
        }

        @Override
        public int getItemLayoutId(int viewType) {
            return R.layout.item_load_command_list;
        }
    }

}
