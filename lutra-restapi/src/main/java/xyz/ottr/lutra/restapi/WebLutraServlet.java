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

public class WebLutraServlet extends HttpServlet {

    private static final long serialVersionUID = -7342968018534639139L;

    private static final int MAX_SIZE = 50000;
    private static final List<String> originWhitelist = Arrays.asList("http://weblutra.ottr.xyz");

    /*
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) {
        String origin = request.getHeader("Origin");
        setAccessControlHeaders(response, origin);
        response.setStatus(HttpServletResponse.SC_OK);
    }
    */

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doIt(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doIt(request, response);
    }

    private void doIt(HttpServletRequest request, HttpServletResponse response) throws IOException {

        CLIWrapper cli = new CLIWrapper();

        String input = request.getParameter("input");
        String library = request.getParameter("library");

        cli.setInput(input);
        cli.setInputFormat(request.getParameter("inputFormat"));
        cli.setLibrary(library);
        cli.setOutputFormat(request.getParameter("outputFormat"));
        cli.setLibraryFormat(request.getParameter("libraryFormat"));

        String output;

        if (input.length() > MAX_SIZE || library.length() > MAX_SIZE) {
            output = "Error. Input exceeds max input size, please use desktop version of Lutra.";
        } else {
            try {
                output = cli.run();
            } catch (Exception ex) {
                output = "Error."
                    + "\n\nMessage: " + ex.getMessage();
            }
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
        //response.setHeader("Access-Control-Allow-Credentials", "true");
        //response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE, HEAD");
        //response.setHeader("Access-Control-Max-Age", "3600");
        //response.setHeader("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, X-Requested-With");
    }

    private void writeResponse(HttpServletResponse response, String content) throws IOException {
        response.setContentType("text/plain");
        //response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter writer = response.getWriter()) {
            writer.println(content);
            writer.println("### " + OffsetDateTime.now(ZoneOffset.UTC));
        }
    }

}
