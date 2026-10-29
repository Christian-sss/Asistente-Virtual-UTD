package asistente.servicio;

import asistente.config.Configuracion;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Cliente del servicio local ClamAV (clamd) mediante el comando INSTREAM (TCP 3310).
 */
public class ClamAvCliente {

    public enum Resultado { LIMPIO, INFECTADO, NO_DISPONIBLE }

    private static final int BLOQUE = 8192;

    public Resultado analizar(byte[] contenido) {
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress(Configuracion.clamavHost(), Configuracion.clamavPuerto()), 1500);
            s.setSoTimeout(20000);
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            out.write("zINSTREAM\0".getBytes(StandardCharsets.US_ASCII));
            for (int i = 0; i < contenido.length; i += BLOQUE) {
                int n = Math.min(BLOQUE, contenido.length - i);
                out.writeInt(n);
                out.write(contenido, i, n);
            }
            out.writeInt(0);
            out.flush();
            InputStream in = s.getInputStream();
            String respuesta = new String(in.readAllBytes(), StandardCharsets.US_ASCII).trim();
            return respuesta.endsWith("OK") ? Resultado.LIMPIO : Resultado.INFECTADO;
        } catch (IOException e) {
            return Resultado.NO_DISPONIBLE;
        }
    }
}
