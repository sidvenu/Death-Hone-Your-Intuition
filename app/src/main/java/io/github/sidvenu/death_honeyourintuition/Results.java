package io.github.sidvenu.death_honeyourintuition;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.SineOscillator;
import com.jsyn.unitgen.UnitOscillator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class Results {

    private static final String USER_AGENT = "Mozilla/5.0";

    private static final String POST_URL = "https://results.nitt.edu/results.php";

    private static final String POST_PARAMS = "uname=107118096&pwd=sidrules15";

    public static void main(String[] args) {
        final Timer t = new Timer();

        t.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            String result = sendPOST();
                            File f = new File("C:\\Users\\SiddharthVenu\\Desktop\\result.html");
                            PrintWriter writer = new PrintWriter(f);
                            writer.print(result);
                            writer.close();
                            if (result.contains("not declared so far") || !result.replaceFirst("<tr>", "").contains("<tr>")) {
                                System.out.println("NOT DECLARED YET\n");
                            } else {
                                System.out.println("CHECK RESULTSSSS\n");
                                test();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                },0,
                30000
        );

    }

    static Synthesizer synth;
    static UnitOscillator osc;
    static LineOut lineOut;

    private static void test() {

        // Create a context for the synthesizer.
        synth = JSyn.createSynthesizer();

        // Start synthesizer using default stereo output at 44100 Hz.
        synth.start();

        // Add a tone generator.
        synth.add(osc = new SineOscillator());
        // Add a stereo audio output unit.
        synth.add(lineOut = new LineOut());

        // Connect the oscillator to both channels of the output.
        osc.output.connect(0, lineOut.input, 0);
        osc.output.connect(0, lineOut.input, 1);

        // Set the frequency and amplitude for the sine wave.
        osc.frequency.set(345.0);
        osc.amplitude.set(0.6);

        // We only need to start the LineOut. It will pull data from the
        // oscillator.
        lineOut.start();

        System.out.println("You should now be hearing a sine wave. ---------");

        // Sleep while the sound is generated in the background.
        try {
            double time = synth.getCurrentTime();
            System.out.println("time = " + time);
            // Sleep for a few seconds.
            synth.sleepUntil(time + 4.0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Stop playing. -------------------");
        // Stop everything.
        synth.stop();
    }


    private static String sendPOST() throws IOException {
        URL obj = new URL(POST_URL);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);

        // For POST only - START
        con.setDoOutput(true);
        OutputStream os = con.getOutputStream();
        os.write(POST_PARAMS.getBytes());
        os.flush();
        os.close();
        // For POST only - END

        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.US);
        System.out.println('\n'+formatter.format(new Date()));

        int responseCode = con.getResponseCode();
        System.out.println("POST Response Code :: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) { //success
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // print result
            return response.toString();
        } else {
            return "POST request not worked";
        }
    }
}
