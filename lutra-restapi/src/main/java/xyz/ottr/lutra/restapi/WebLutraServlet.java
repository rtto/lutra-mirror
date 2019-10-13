package xyz.ottr.lutra.restapi;

/*-
 * #%L
 * lutra-restapi
 * %%
 * Copyright (C) 2018 - 2019 University of Oslo
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.io.IOException;
import java.io.PrintWriter;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ottr.lutra.bottr.BOTTR;
import xyz.ottr.lutra.result.Message;

public class WebLutraServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(WebLutraServlet.class);

    private static final long serialVersionUID = -7342968018534639139L;

    private static final List<String> originWhitelist = Arrays.asList("http://weblutra.ottr.xyz");

    private static final long MAX_REQUEST_SIZE = 600000;
    private static final long MAX_FILE_SIZE = 100000;


    static {
        BOTTR.Settings.setRDFSourceQueryLimit(200);
    }

    public ServletFileUpload initServletFileUpload() {
        DiskFileItemFactory factory = new DiskFileItemFactory();
        // Use default values:
        //factory.setSizeThreshold(MEMORY_THRESHOLD);
        //factory.setRepository(new File(System.getProperty("java.io.tmpdir")));

        ServletFileUpload uploader = new ServletFileUpload(factory);
        uploader.setFileSizeMax(MAX_FILE_SIZE);
        uploader.setSizeMax(MAX_REQUEST_SIZE);

        return uploader;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            doIt(request, response);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new IOException(e);
        }
    }

    private void doIt(HttpServletRequest request, HttpServletResponse response) throws Exception {

        CLIWrapper cli = new CLIWrapper();

        ServletFileUpload uploader = initServletFileUpload();
        List<FileItem> fileItems = uploader.parseRequest(request);

        if (fileItems != null) {

            // Must set prefixes first so prefixes are prepended to input and library.
            fileItems.stream()
                .filter(fi -> fi.getFieldName().equalsIgnoreCase("prefixes"))
                .findFirst()
                .ifPresent(fi -> cli.setPrefixes(fi.getString()));

            for (FileItem item : fileItems) {
                switch (item.getFieldName()) {

                    // files
                    case "fileInput" :
                        cli.addInput(item);
                        break;
                    case "fileLibrary" :
                        cli.addLibrary(item);
                        break;
                    case "fileData" :
                        cli.addData(item);
                        break;

                    // form input
                    case "input":
                        cli.addInput(item.getString());
                        break;
                    case "library":
                        cli.addLibrary(item.getString());
                        break;
                    case "mode":
                        cli.setMode(item.getString());
                        break;
                    case "fetchMissing":
                        cli.setFetchMissing("true".equalsIgnoreCase(item.getString()));
                        break;
                    case "inputFormat":
                        cli.setInputFormat(item.getString());
                        break;
                    case "outputFormat":
                        cli.setOutputFormat(item.getString());
                        break;
                    case "libraryFormat":
                        cli.setLibraryFormat(item.getString());
                        break;
                    default:
                        break;
                }
            }
        }

        String output;
        try {
            output = cli.run();
        } catch (Exception ex) {
            output = Message.error(ex.getMessage()).toString();
        }

        String origin = request.getHeader("Origin");
        if (originWhitelist.contains(origin)) {
            setAccessControlHeaders(response, origin);
        }
        writeResponse(response, output);
    }

    private static void setAccessControlHeaders(HttpServletResponse response, String origin) {
        response.setHeader("Access-Control-Allow-Origin", origin);
        response.setHeader("Vary", "Origin");
    }

    private void writeResponse(HttpServletResponse response, String content) throws IOException {
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter writer = response.getWriter()) {
            writer.println(content);
            writer.println("### " + OffsetDateTime.now(ZoneOffset.UTC));
        }
    }

}
