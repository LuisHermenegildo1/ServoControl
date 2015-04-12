package com.example.servocontrol;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;


@SuppressLint("NewApi") public class MainActivity extends ListActivity {
	private static final String TAG = "ToggleLed";
	private static final int ENABLE_BLUETOOTH = 1;
	
	private BluetoothAdapter mBluetoothAdapter; //Se encarga de todo lo correspondiente al bluetooth
	private ArrayList<BluetoothDevice> btDeviceList = new ArrayList<BluetoothDevice>(); //Almacena los dispositivos descubiertos
	private BluetoothSocket clientSocket; //Establece la conexión entre Android y Arduino
	private BroadcastReceiver discoveryMonitor; //Detecta los nuevos dispositivos
	
	
	private ArrayAdapter<String> mArrayAdapter;
	private Button buttonToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    	// Instance AsyncTask
 		//connectAsyncTask = new ConnectAsyncTask();
 		
 		//Obtenemos el adapatador Bluetoth
 		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
 		
 		// Verificamos si nuestro dispositivo soporta el bluetooth
 		if(mBluetoothAdapter == null){
 			//En caso de no soportarlo lo finaliza
 			Toast.makeText(getApplicationContext(), "Not support bluetooth", 5).show();
 			finish();
 		}
        
		// Verificamos si el Bluetooth esta habilitado mediante un intent
		if(!mBluetoothAdapter.isEnabled()){
			//En caso de que no manda una petición para activarlo
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, 1);
		}

		// Componentes de la interface grafica.
		mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		setListAdapter(mArrayAdapter);
		
		//buttonToggle = (Button) findViewById(R.id.btToggle);
		//buttonToggle.setOnClickListener(buttonToggleOnClickListener);
    }

    
	@Override
	protected void onResume() {
		super.onResume();
		
		if(this.discoveryMonitor !=null){
		    registerReceiver(discoveryMonitor, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
		    registerReceiver(discoveryMonitor, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
		    registerReceiver(discoveryMonitor, new IntentFilter(BluetoothDevice.ACTION_FOUND));
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
			
		if (requestCode == ENABLE_BLUETOOTH){
			if (resultCode == RESULT_OK){
				Log.d(TAG, "Bluetooth: el usuario acepta encenderlo");
				// Ejecutamos el metodo dicoveryBluetooth
				dicoveryBluetooth();
			}else{
				Log.d(TAG, "Bluetooth: el usuario NO acepta encenderlo");
			}
		}
	}
	
	private void dicoveryBluetooth() {	
		
		// Limpiamos la lista de dispositivos detectados.
		mArrayAdapter.clear();
		btDeviceList.clear();
		
		// Aqui implementamos el BrodcastReceiver
		this.discoveryMonitor = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
			
				// TODO: Acciones, al iniciar el dicovery, finalizar y cuando encuentra un dispositivo
				
				if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(intent.getAction())){
				    Log.d(TAG, "Discovery started...");
				}else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())){
				    Log.d(TAG, "Discovery complete.");
				}else if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())){

				    // Añadimos el dispositivo encontrado al adaptador del ListView.
				    String remoteName = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
				    mArrayAdapter.add(remoteName);
				    Log.d(TAG, "Dispositivo detectado :" + remoteName);

				    // Recuperamos el dispositivo detectado y lo guardamos en el array de dispositivos.
				    BluetoothDevice remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				    btDeviceList.add(remoteDevice);
				}				
			}		
		};
		
		// TODO: Iniciamos la busqueda de dispositvos bluetooth.
		// Este metodo es muy lento y consumo mucha bateria
		// en otros capitulos veremos como usar otra tecnica.
		mBluetoothAdapter.startDiscovery();
	
	}

	@Override
	protected void onPause() {
		super.onPause();
		
		if (this.discoveryMonitor != null){
			unregisterReceiver(discoveryMonitor);
		}
		
	}
	
	private boolean connectRemoteDevice(BluetoothDevice device){
		
		Log.d(TAG, "Connectando");
		boolean connect = false;
		
		// TODO: Conexion socket cliente.
		try {
		    String mmUUID = "00001101-0000-1000-8000-00805F9B34FB";
		    this.clientSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(mmUUID));
		    clientSocket.connect();
		    connect = true;
		} catch (Exception e) {
		    Log.d(TAG,e.getMessage());
		    connect = false;
		}

		return connect;
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		
		BluetoothDevice device = btDeviceList.get(position);
		Log.d(TAG, "Dispositivo seleccionado: "  + device.getName());
		
		// Intentamos conectar con el dispositivo remoto.
		if(connectRemoteDevice(device)){
			buttonToggle.setEnabled(true);
		}
		
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private OnClickListener buttonToggleOnClickListener = new OnClickListener() {
    	
    	@Override
    	public void onClick(View v) {
    	
    		Log.d(TAG, "Enviando cambio de estado del LED");
    		
    		// TODO: Enviando informacion del Movil hacia el Arduino.
    		OutputStream mmOutStream = null;
    		try {
    		    if (clientSocket.isConnected()){
    		        mmOutStream = clientSocket.getOutputStream();
    		        mmOutStream.write(new String("L").getBytes());
    		    }else{
    		        Toast.makeText(getApplicationContext(), "Not connected",0).show();
    		    }
    		} catch (IOException e) {
    		    Log.d(TAG,e.getMessage());
    		    buttonToggle.setEnabled(false);
    		}
    		
    	}
    };
    
    
}


