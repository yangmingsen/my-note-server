package top.yms.note.conpont.http;

import org.springframework.stereotype.Component;
import top.yms.note.conpont.crawler.impl.ProxyFactory;
import top.yms.note.conpont.crawler.impl.UserAgentProvider;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

@Component
public class DefaultNoteHttpRequestServiceImpl implements NoteHttpRequestService{
    @Override
    public InputStream openImageStream(String imgUrl) throws IOException {
        URL url = new URL(imgUrl);
        HttpURLConnection conn =
                (HttpURLConnection) url.openConnection(ProxyFactory.http());
        conn.setConnectTimeout(10_000);
        conn.setReadTimeout(15_000);
        conn.setRequestProperty("User-Agent", UserAgentProvider.getUserAgent());
        conn.setRequestProperty("Accept",
                "image/avif,image/webp,image/apng,image/*,*/*;q=0.8");
        conn.setRequestProperty("Referer", imgUrl);
        conn.setInstanceFollowRedirects(true);

        return conn.getInputStream();
    }
}
