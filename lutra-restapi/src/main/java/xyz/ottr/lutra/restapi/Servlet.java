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
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;

public class Servlet extends HttpServlet {

    private static final long serialVersionUID = -7342968018534639139L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        CLIWrapper cli = new CLIWrapper();

        cli.setInput(request.getParameter("input"));
        cli.setInputFormat(request.getParameter("inputFormat"));
        cli.setLibrary(request.getParameter("library"));
        cli.setOutputFormat(request.getParameter("outputFormat"));
        cli.setLibraryFormat(request.getParameter("libraryFormat"));

        String output = "";
        try {
            output = cli.run();
        } catch (Exception ex) {
            output = "Error!\n\n"
                + ex.getMessage()
                + "\n\n"
                + ExceptionUtils.getStackTrace(ex);
        }

        writeResponse(response, output);
    }

    private void writeResponse(HttpServletResponse response, String content) throws IOException {
        response.setContentType("text/plain");
        //response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();

        writer.append(content);
    }

}
