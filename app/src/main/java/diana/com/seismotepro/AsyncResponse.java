package diana.com.seismotepro;

/**
 * Created by Diana Scurati on 14/01/2016.
 * questa interfaccia e utile come "connettore" tra le async task e i thread principale. se si implementa il metodo
 * processFinish quando viene chiamata process finish riceve la stringa dalla relativa async task,
 */
public interface AsyncResponse {

    void processFinish(Boolean output);
}