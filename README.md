[![Release](https://jitpack.io/v/erichlotto/bt-helper-android.svg)](https://jitpack.io/#erichlotto/bt-helper-android)

Essa é uma biblioteca para facilitar a comunicação com dispositivos Bluetooth (testada apenas em arduinos).
O dispositivo remoto age como host, é necessário que o Socket seja criado por ele.

## 1. Adicionando a dependência

Adicionar ao build.gradle do projeto:
```gradle
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```
Adicionar a dependência ao módulo do app:
```gradle
dependencies {
  compile 'com.github.erichlotto:bt-helper-android:v1.0'
}
```

## 2. Usando a biblioteca
Antes de mais nada, adicione as permissões ao manifesto do seu projeto:
```xml
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
```

Em seguida, em sua atividade:
```java
BTHelper btHelper = new BTHelper(this);
```
Para exibir o diálogo de seleço de dispositivos:
```java
btHelper.showConnectionDialog(new ConnectionThread.OnConnectionListener() {
  @Override
  public void onConnected(BluetoothDevice bluetoothDevice, BluetoothSocket bluetoothSocket) {
    System.out.println("Conectou em " + bluetoothDevice.getName());
      SendCommandThread command = new SendCommandThread(bluetoothSocket);
      command.start();
  }

  @Override
  public void onError(Exception e) {
    e.printStackTrace();
  }
});
```

Sempre lembre de chamar o método onDestroy():
```java
@Override
protected void onDestroy() {
  btHelper.onDestroy();
  super.onDestroy();
}
```

Para transmitir dados, crie uma Thread que extenda `AbstractCommandThread` e implemente a lógica desejada no método run(). Você pode enviar dados para o dispositivo usando o método `mmOutStream.write()`:
```java
public class SendCommandThread extends AbstractCommandThread {

  public SendCommandThread(BluetoothSocket socket) {
    super(socket);
  }


  public void run() {
    while(!isInterrupted()){
      byte[] buffer = new byte[7];  // buffer store for the stream

      buffer[0] = (byte)'*';
      buffer[1] = (byte)(0xff00>>8);
      buffer[2] = (byte)0x00ff;
      buffer[3] = (byte)(0xff00>>8);
      buffer[4] = (byte)0x00ff;
      buffer[5] = (byte)0;
      buffer[6] = (byte)0;

      try {
        mmOutStream.write(buffer);
        sleep(20);
      } catch (InterruptedException | IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
}
```
