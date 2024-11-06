package servent;

import app.AppConfig;
import app.Cancellable;
import servent.base_handler.*;
import servent.base_message.Message;
import servent.base_message.util.MessageUtil;
import servent.storage_handler.*;
import servent.storage_message.AskPullMessage;
import servent.storage_message.TellPullMessage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimpleServentListener implements Runnable, Cancellable {

    private volatile boolean working = true;

    public SimpleServentListener() {

    }

    /*
     * Thread pool for executing the handlers. Each client will get it's own handler thread.
     */
    private final ExecutorService threadPool = Executors.newWorkStealingPool();

    @Override
    public void run() {
        ServerSocket listenerSocket = null;
        try {
            listenerSocket = new ServerSocket(AppConfig.myServentInfo.getListenerPort(), 100);
            /*
             * If there is no connection after 1s, wake up and see if we should terminate.
             */
            listenerSocket.setSoTimeout(1000);
        } catch (IOException e) {
            AppConfig.timestampedErrorPrint("Couldn't open listener socket on: " + AppConfig.myServentInfo.getListenerPort());
            System.exit(0);
        }


        while (working) {
            try {
                Message clientMessage;

                Socket clientSocket = listenerSocket.accept();

                //GOT A MESSAGE! <3
                clientMessage = MessageUtil.readMessage(clientSocket);

                MessageHandler messageHandler = new NullHandler(clientMessage);


                switch (clientMessage.getMessageType()) {
                    case NEW_NODE:
                        messageHandler = new NewNodeHandler(clientMessage);
                        break;
                    case WELCOME:
                        messageHandler = new WelcomeHandler(clientMessage);
                        break;
                    case SORRY:
                        messageHandler = new SorryHandler(clientMessage);
                        break;
                    case UPDATE:
                        messageHandler = new UpdateHandler(clientMessage);
                        break;
                    case TOKEN:
                        messageHandler = new TokenHandler(clientMessage);
                        break;
                    case JOINED:
                        messageHandler = new JoinedHandler(clientMessage);
                        break;
				case ASK_PULL:
                    messageHandler = new AskPullHandler(clientMessage);
					break;
				case TELL_PULL:
                    messageHandler = new TellPullHandler(clientMessage);
					break;
				case REMOVE:
					messageHandler = new RemoveHandler(clientMessage);
					break;
                    case ADD_INFORM:
                        messageHandler = new AddInformHandler(clientMessage);
                        break;

                    case POISON:
                        break;
                }

                threadPool.submit(messageHandler);
            } catch (SocketTimeoutException timeoutEx) {
                //Uncomment the next line to see that we are waking up every second.
//				AppConfig.timedStandardPrint("Waiting...");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void stop() {
        this.working = false;
    }

}
