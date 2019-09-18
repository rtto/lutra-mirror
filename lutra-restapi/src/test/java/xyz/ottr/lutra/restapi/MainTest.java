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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Test;

public class MainTest extends JerseyTest {

    @Override
    public Application configure() {
        return new ResourceConfig(Main.class);
    }

    @Test
    public void testStottr() throws ExecutionException, InterruptedException {
        Form form = new Form();
        form.param("input", "@prefix ottr:  <http://ns.ottr.xyz/0.4/> .\nottr:Triple(<http://example.com>, <http://example.com>, <http://example.com>) . ");
        form.param("inFormat", "stottr");
        form.param("outFormat", "stottr");

        WebTarget target = target("/expand");

        Future<String> response = target.request(MediaType.APPLICATION_FORM_URLENCODED)
            .accept(MediaType.TEXT_PLAIN)
            .buildPost(Entity.form(form))
            .submit(String.class);

        Assert.assertThat(response.get(), is(notNullValue()));
    }

}

