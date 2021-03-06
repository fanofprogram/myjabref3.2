/*  Copyright (C) 2003-2015 JabRef contributors.
    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
///////////////////////////////////////////////////////////////////////////////
//  Filename: $RCSfile$
//  Purpose:  Atom representation.
//  Language: Java
//  Compiler: JDK 1.4
//  Authors:  Joerg K. Wegner
//  Version:  $Revision$
//            $Date$
//            $Author$
//
//  Copyright (c) Dept. Computer Architecture, University of Tuebingen, Germany
//
//  This program is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation version 2 of the License.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
///////////////////////////////////////////////////////////////////////////////
package net.sf.jabref.exporter.layout;

import java.util.List;
import java.util.StringTokenizer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JabRef helper methods.
 *
 * @author     wegnerj
 */
public final class WSITools
{

    private static final Log LOGGER = LogFactory.getLog(WSITools.class);


    private WSITools()
    {
    }

    /**
     * @param  list  {@link java.util.List} of <tt>String</tt>
     * @param  buf  Description of the Parameter
     * @return      Description of the Return Value
     */
    public static boolean tokenize(List<String> list, String buf)
    {
        return WSITools.tokenize(list, buf, " \t\n");
    }

    /**
     * @param  list       {@link java.util.List} of <tt>String</tt>
     * @param  buf       Description of the Parameter
     * @param  delimstr  Description of the Parameter
     * @return           Description of the Return Value
     */
    public static boolean tokenize(List<String> list, String buf, String delimstr)
    {
        list.clear();
        buf = buf + '\n';

        StringTokenizer st = new StringTokenizer(buf, delimstr);

        while (st.hasMoreTokens())
        {
            list.add(st.nextToken());
        }

        return true;
    }

    /**
     * @param  list       {@link java.util.List} of <tt>String</tt>
     * @param  s         Description of the Parameter
     * @param  delimstr  Description of the Parameter
     * @param  limit     Description of the Parameter
     * @return           Description of the Return Value
     */
    public static boolean tokenize(List<String> list, String s, String delimstr,
            int limit)
    {
        LOGGER.warn("Tokenize \"" + s + '"');
        list.clear();
        s = s + '\n';

        int endpos;
        int matched = 0;

        StringTokenizer st = new StringTokenizer(s, delimstr);

        while (st.hasMoreTokens())
        {
            String tmp = st.nextToken();
            list.add(tmp);

            matched++;

            if (matched == limit)
            {
                endpos = s.lastIndexOf(tmp);
                list.add(s.substring(endpos + tmp.length()));

                break;
            }
        }

        return true;
    }
}
