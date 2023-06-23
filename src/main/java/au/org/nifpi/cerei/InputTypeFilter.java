/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */ 

package au.org.nifpi.cerei;

import java.io.File;
import javax.swing.filechooser.*;

/**
 * Input files should have .csv extension.  Minor cosmetic changes to Oracle's sample code.
 * Used by InuptFile.java
 * ImageFilter.java is used by FileChooserDemo2.java.
 * 
 * @author Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 */
public class InputTypeFilter extends FileFilter {

    /**
     * Default constructor
     */
	public InputTypeFilter() {
		
	}
	
	/**
	 * Accept all directories and all csv files.
	 * 
	 * @param f File under consideration
	 * 
	 * @return True is file is a directory or has type .csv, otherwise False.
	 */
	public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension = Utils.getExtension(f);
        if (extension != null) {
            if (extension.equals(Utils.csv))  {
                    return true;
            } else {
                return false;
            }
        }

        return false;
    }

	/**
	 * Text to go in File chooser - the description of this filter
	 * 
	 * @return Text to go in File chooser
	 */
    public String getDescription() {
        return "Only .csv files";
    }
}