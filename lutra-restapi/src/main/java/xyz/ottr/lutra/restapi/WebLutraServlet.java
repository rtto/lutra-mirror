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
import java.util.ArrayList;
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
import xyz.ottr.lutra.system.Message;

public class WebLutraServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(WebLutraServlet.class);

    private static final long serialVersionUID = -7342968018534639139L;

    private static final List<String> originWhitelist;

    static {
        var domains = new ArrayList<String>();
        List.of("", "weblutra.", "www.", "spec.", "primer.", "dev.spec.").stream()
            .forEach(sub -> {
                domains.add("http://" + sub + "ottr.xyz");
                domains.add("https://" + sub + "ottr.xyz");
            });
        originWhitelist = domains;
    }

    /*
    private static final String repoLibrary = "https://gitlab.com/ottr/templates.git";

    private static final String attrLibraryRepo = "libraryRepo";
    private static final String attrLastPullTime = "lastPullTime";

    private static final long pullInterval = 1000 * 60 * 10; // update repo every 10 mins
    */

    private static final long MAX_FILE_SIZE = 100 * 1024;
    private static final long MAX_REQUEST_SIZE = 5 * MAX_FILE_SIZE;

    static {
        BOTTR.Settings.setRDFSourceQueryLimit(200);
    }

    /*
    private void updateLibrary() throws IOException, GitAPIException {

        ServletContext context = getServletContext();

        File repo = (File) context.getAttribute(attrLibraryRepo);

        // clone
        if (repo == null) {
            repo = Files.createTempDirectory("tplLibrary").toFile();
            Git.cloneRepository()
                .setURI(repoLibrary)
                .setDirectory(repo)
                .call();

            getServletContext().setAttribute(attrLibraryRepo, repo);
        }

        long lastPullTime = (Long) ObjectUtils.defaultIfNull(context.getAttribute(attrLastPullTime), 0L);
        long now = System.currentTimeMillis();

        // pull
        if (now > lastPullTime + this.pullInterval) {
            context.setAttribute(attrLastPullTime, now);

            Git git = Git.open(repo);
            git.pull();
        }
    }*/

    private ServletFileUpload initServletFileUpload() {
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
                        cli.addInputFile(item);
                        break;
                    case "fileLibrary" :
                        cli.addLibraryFile(item);
                        break;
                    case "fileData" :
                        cli.addDataFile(item);
                        break;

                    // form input
                    case "input":
                        cli.addInputString(item.getString());
                        break;
                    case "library":
                        cli.addLibraryString(item.getString());
                        break;
                    case "mode":
                        cli.setMode(item.getString());
                        break;
                    // TODO disable fetching to protect tpl.ottr.xyz server
                    //case "fetchMissing":
                    //    cli.setFetchMissing("true".equalsIgnoreCase(item.getString()));
                    //    break;
                    //case "loadStdLib":
                    //    cli.setLoadTplLibrary("true".equalsIgnoreCase(item.getString()));
                    //    break;
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
