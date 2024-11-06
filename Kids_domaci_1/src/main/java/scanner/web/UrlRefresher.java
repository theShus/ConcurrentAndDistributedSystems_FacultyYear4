package scanner.web;

import java.util.Map;

public class UrlRefresher extends Thread {

    private final Map<String, Long> scannedUrls;
    private boolean running = true;


    public UrlRefresher(Map<String, Long> scannedUrls) {
        this.scannedUrls = scannedUrls;
    }


    @Override
    public void run() {//zavrti se svahih neko vreme, prodje kroz mapu skeniranih url-ova i skloni ih ako im je istekao refresh block
        while (running) {

            for (Map.Entry<String, Long> scannedUrl : scannedUrls.entrySet())
                if (scannedUrl.getValue() < System.currentTimeMillis()) scannedUrls.remove(scannedUrl.getKey());

            try {
                Thread.sleep(1800000);//todo proveri da li je ovo dobro uradnjeno (svakih 30 min proverava dal url treba da se refreshuje)
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void clearDomain(String domain){
        for (Map.Entry<String, Long> url: scannedUrls.entrySet()) {
            if (url.getKey().contains(domain)) scannedUrls.remove(url.getKey());
        }
    }

    public void terminate(){
        running = false;
    }

}
