package com.heroku.janvil;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.file.FileDataBodyPart;

/**
 * @author Ryan Brainard
 */
class CurlFormDataContentDisposition extends FormDataContentDisposition {

    protected CurlFormDataContentDisposition(FormDataContentDisposition d) {
        super(d.getType(), d.getName(), d.getFileName(), d.getCreationDate(), d.getModificationDate(), d.getReadDate(), d.getSize());
    }

    @Override
    protected StringBuilder toStringBuffer() {
        StringBuilder sb = new StringBuilder();

        sb.append(getType());
        addStringParameter(sb, "name", getName());
        addStringParameter(sb, "filename", getFileName());

        return sb;
    }

    public static FileDataBodyPart curlize(FileDataBodyPart fileDataBodyPart) {
        final FormDataContentDisposition original = fileDataBodyPart.getFormDataContentDisposition();
        final CurlFormDataContentDisposition curlized = new CurlFormDataContentDisposition(original);
        fileDataBodyPart.setContentDisposition(curlized);
        return fileDataBodyPart;
    }
}
