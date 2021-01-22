# Aliexpress

Bot scritto in Java 11 per farmare bonus amici di Aliexpress.
Supporta Windows e Linux.

## Metodo

Progetto modulare con GUI e CLI mediante tethering usb con dispostivo Android mediante l'utilizzo di adb

## Setup 
##### Sviluppatori:
Per poter sviluppare su questo branch è necessario:
 1) Installare l'ultima versione di [IntelliJ IDEA](https://www.jetbrains.com/idea/download/)
 2) Installare la [JDK 11](https://adoptopenjdk.net/) 
 3) Installare l'ultima versione di [Apache Maven](https://maven.apache.org/download.cgi)
 4) Abilitare le preview features in IntelliJ IDEA
 5) Scaricare il plugin Lombok da IntelliJ IDEA
 6) Abilitare l'annotation processing da IntelliJ IDEA nelle impostazioni
 7) Assicurarsi di syncare le dipendenze andando nel POM e clickando [sull'icona di Maven](https://imgur.com/a/0eLWHfT) in alto se presente sennò salta questo passaggio
 8) Un dispositivo Android con una SIM il cui piano tariffario comprende i dati mobili

##### Utenti:
Per usare il tool è necessario:
 1) Installare la [JDK 11](https://adoptopenjdk.net/)
 2) Estrarre il contenuto scaricato in C:\Program Files\Java, se la directory non esiste creala
 3) Un dispositivo Android con una SIM il cui piano tariffario comprende i dati mobili

## Per iniziare
Si consiglia di creare max due o tre accounts aliexpress con indirizzi mail reali in questo modo:
1. Apri una nuova finestra in incognito
2. Apri i dev tools clickando su ispeziona elemento, poi in alto clicka sull'icona di un monitor e di un telefono alla sinistra della scritta Elemento
3. Seleziona Pixel 2 dal menu a tendina in alto al centro
4. Recati su aliexpress.com e fai il login
5. Recati [qui](https://campaign.aliexpress.com/wow/gf/cashdailyoutc/index?_addShare=no)
6. Clicka avanti, infine clicka su condividi, copia link e incollalo da qualche parte
7. Chiudi la pagina
8. Ripeti per ogni account

Una volta startato il programma per la prima volta bisogna incollare quei link in  C:\Users<Utente>\aliexpress\config.properties separati da una virgola

## Setup dispositivo android
##### Dispositivi normali:
1) Avvia una volta l'applicazione per generare i file di default che si trovano nella cartella C:\Users\<Utente>\aliexpress per windows e /home/aliexpress per Linux
2) Disattiva notifiche e sospensione dello schermo
3) Vai nelle impostazioni e attiva le [impostazioni da sviluppatore](https://www.wikihow.it/Abilitare-le-%27Opzioni-sviluppatore%27-su-Android)
4) Abilita il Tethering USB, Debug USB e Posizione Puntatore
5) Nelle impostazioni trova il pulsante per la modalità aereo e segnati le coordinate
6) Disattiva Posizione Puntatore
7) Apri il file ip.bat nella cartella resources, cancella tutto e incolla:
   ```
   adb shell input tap x y
   ping 127.0.0.1 -n 6 > nul
   adb shell input tap x y
   ```
   dove x e y sono le coordinate precedentemente annotate
   
##### Dispositivi rooted:
1) Avvia una volta l'applicazione per generare i file di default che si trovano nella cartella C:\Users\<Utente>\aliexpress per windows e /home/aliexpress per Linux
2) Vai nelle impostazioni e attiva le [impostazioni da sviluppatore](https://www.wikihow.it/Abilitare-le-%27Opzioni-sviluppatore%27-su-Android)
4) Abilita il Tethering USB e il Debug USB
5) Apri il file ip.bat nella cartella resources, cancella tutto e incolla:
   ```
   adb root

   adb shell settings put global airplane_mode_on 1
   adb shell am broadcast -a android.intent.action.AIRPLANE_MODE

   ping 127.0.0.1 -n 6 > nul

   adb shell settings put global airplane_mode_on 0
   adb shell am broadcast -a android.intent.action.AIRPLANE_MODE
   ```

## Prima di avviare l'applicazione
1) Assicurati di aver modificato il file C:\Users\<Utente>\aliexpress\config.properties per windows e /home/aliexpress/config.properties per Linux con i tuoi link e preferenze
2) Se il tuo dispositivo non è roottato lascialo nella schermata della modalità aereo come quando hai preso le coordinate

## Informazioni utili per gli sviluppatori

1) Tutti i file che volete aggiungere che non hanno come estensione .java vanno inseriti nella cartella resources
2) Tutte le configurazioni presenti nella cartella resources sono di default, quelle utilizzate dall'applicazione vengono salvate nella cartella C:\Users\<Utente>\aliexpress per windows e /home/aliexpress per Linux
3) Tutti i test vanno inseriti nella cartella test/java, come fatto per Adb.java