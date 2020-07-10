package xyz.ottr.lutra.docttr;

/*-
 * #%L
 * xyz.ottr.lutra:lutra-docttr
 * %%
 * Copyright (C) 2018 - 2020 University of Oslo
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

import static j2html.TagCreator.document;
import static j2html.TagCreator.html;
import static j2html.TagCreator.rawHtml;

public class HTMLFramesetWriter {

    public String write() {

        var head = HTMLFactory.getHead("Library of Reasonable Ontology Templates (OTTR)");

        var frameset = rawHtml(
            "<frameset cols=\"25%,75%\" border=\"1px\" bordercolor=\"#ccc\">"
                + "<frame src=\"" + DocttrManager.FILENAME_MENU + "\" name=\"menu-frame\" title=\"Menu\">"
                + "<frame src=\"" + DocttrManager.FILENAME_FRONTPAGE + "\" name=\"main-frame\" title=\"Main\" scrolling=\"yes\">"
                + "<noframes>"
                + "  <noscript><div>JavaScript is disabled on your browser.</div></noscript>"
                + "  <h2>Frame Alert</h2>"
                + "  <p>This document is designed to be viewed using the frames"
                + "  feature. If you see this message, you are using a"
                + "  non-frame-capable web client. Link to <a href=\"" + DocttrManager.FILENAME_FRONTPAGE + "\">non-frame version</a>.</p>"
                + "</noframes>"
                + "</frameset>");

        return document(html(head, frameset));
    }

}
