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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.FileUtils;
import xyz.ottr.lutra.cli.CLI;


@Path("/expand")
public class Main {

    private static final String CHARSET = "UTF-8";

    @GET
    public String message() {
        return "Hello!";
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public String lutra(
        @FormParam("input") String input,
        @FormParam("inFormat") String inFormat,
        @FormParam("outFormat") String outFormat
    ) throws IOException {

        java.nio.file.Path tempInput = Files.createTempFile("input-", ".tmp");

        FileUtils.write(tempInput.toFile(), input, CHARSET);
        String tempInputPath = tempInput.toAbsolutePath().toString();

        String command = "--mode expand"
            + " --inputFormat " + inFormat
            + " --outputFormat " + outFormat
            //+ " --library"
            + " --fetchMissing"
            + " --stdout "
            + tempInputPath;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos, true, CHARSET);

        new CLI(ps).run(command.split(" "));

        String output = baos.toString(CHARSET);

        // clean up
        tempInput.toFile().deleteOnExit();

        return output
            + "\n\n ### The CLI command used:"
            + "\n ### "  + command;
    }
}

