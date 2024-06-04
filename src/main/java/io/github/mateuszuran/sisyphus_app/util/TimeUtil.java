package io.github.mateuszuran.sisyphus_app.util;

import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class TimeUtil {

    public String formatCreationTime() {
        SimpleDateFormat formattedDate = new SimpleDateFormat("dd-MM-yyyy");
        return formattedDate.format(new Date());
    }
}
