package top.yms.note.conpont.store;

import top.yms.note.conpont.AnyFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class NewMongoStoreFile implements AnyFile {

    private SmallFileDocument smallFileDocument;

    public NewMongoStoreFile(SmallFileDocument smallFileDocument) {
        this.smallFileDocument = smallFileDocument;
    }

    @Override
    public long writeTo(OutputStream out) throws IOException {
        out.write(smallFileDocument.getData());
        return smallFileDocument.getSize();
    }

    @Override
    public long getLength() {
        return smallFileDocument.getSize();
    }

    @Override
    public String getContentType() {
        return smallFileDocument.getContentType();
    }

    @Override
    public String getFilename() {
        return smallFileDocument.getFilename();
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(smallFileDocument.getData());
    }
}
