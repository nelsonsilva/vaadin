/* *************************************************************************
 
 IT Mill Toolkit 

 Development of Browser User Interfaces Made Easy

 Copyright (C) 2000-2006 IT Mill Ltd
 
 *************************************************************************

 This product is distributed under commercial license that can be found
 from the product package on license.pdf. Use of this product might 
 require purchasing a commercial license from IT Mill Ltd. For guidelines 
 on usage, see licensing-guidelines.html

 *************************************************************************
 
 For more information, contact:
 
 IT Mill Ltd                           phone: +358 2 4802 7180
 Ruukinkatu 2-4                        fax:   +358 2 4802 7181
 20540, Turku                          email:  info@itmill.com
 Finland                               company www: www.itmill.com
 
 Primary source for information and releases: www.itmill.com

 ********************************************************************** */

package com.itmill.toolkit.terminal.web;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Theme source for reading themes from a JAR archive. At this time only jar
 * files are supported and an archive may not contain any recursive archives.
 * 
 * @author IT Mill Ltd.
 * @version
 * @VERSION@
 * @since 3.0
 */
public class JarThemeSource implements ThemeSource {

	private File file;

	private JarFile jar;

	private Theme theme;

	private String path;

	private String name;

	private ApplicationServlet webAdapterServlet;

	private Cache resourceCache = new Cache();

	/** 
	 * Collection of subdirectory entries. 
	 */
	private Collection subdirs = new LinkedList();

	/**
	 * Creates a new instance of ThemeRepository by reading the themes from a
	 * local directory.
	 * 
	 * @param file
	 *            the Path to the JAR archive .
	 * @param webAdapterServlet
	 * @param path
	 *            the Path inside the archive to be processed.
	 * @throws ThemeException 
	 * 					If the resource is not found or there was
	 * 			 			  some problem finding the resource.
	 * 
	 * @throws FileNotFoundException
	 *             if no theme files are found.
	 * @throws IOException
	 * 				if the writing failed due to input/output error.
	 */
	public JarThemeSource(File file, ApplicationServlet webAdapterServlet,
			String path) throws ThemeException, FileNotFoundException,
			IOException {

		this.file = file;
		this.jar = new JarFile(file);
		this.theme = null;
		this.path = path;
		if (this.path.length() > 0 && !this.path.endsWith("/")) {
			this.path = this.path + "/";
		}
		this.name = file.getName();
		if (this.name.toLowerCase().endsWith(".jar")) {
			this.name = this.name.substring(0, this.name.length() - 4);
		}

		this.webAdapterServlet = webAdapterServlet;

		// Loads description file
		JarEntry entry = jar.getJarEntry(this.path + Theme.DESCRIPTIONFILE);
		if (entry != null) {
			try {
				this.theme = new Theme(jar.getInputStream(entry));
			} catch (Exception e) {
				throw new ThemeException("JarThemeSource: Failed to load '"
						+ path + "': ", e);
			}

			// Debug info
			if (webAdapterServlet.isDebugMode(null)) {
				Log.debug("Added JarThemeSource: " + this.file + ":"
						+ this.path);
			}

		} else {
			// There was no description file found.
			// Handle subdirectories recursively
			for (Enumeration entries = jar.entries(); entries.hasMoreElements();) {
				JarEntry e = (JarEntry) entries.nextElement();
				if (e.getName().startsWith(this.path)) {
					if (e.getName().endsWith("/")
							&& e.getName().indexOf('/', this.path.length()) == (e
									.getName().length() - 1)) {
						this.subdirs.add(new JarThemeSource(this.file,
								this.webAdapterServlet, e.getName()));
					}
				}
			}

			if (this.subdirs.isEmpty()) {
				if (webAdapterServlet.isDebugMode(null)) {
					Log.info("JarThemeSource: Ignoring empty JAR path: "
							+ this.file + " path: " + this.path);
				}
			}
		}
	}

	/**
	 * Gets the XSL stream for the specified theme and web-browser type.
	 * @see com.itmill.toolkit.terminal.web.ThemeSource#getXSLStreams(Theme,
	 *      WebBrowser)
	 */
	public Collection getXSLStreams(Theme theme, WebBrowser type)
			throws ThemeException {
		Collection xslFiles = new LinkedList();
		// If this directory contains a theme
		// return XSL from this theme
		if (this.theme != null) {

			if (webAdapterServlet.isDebugMode(null)) {
				Log.info("JarThemeSource: Loading XSL from: " + theme);
			}

			// Reload the theme if JAR has been modified
			JarEntry entry = jar.getJarEntry(this.path + Theme.DESCRIPTIONFILE);
			if (entry != null) {
				try {
					this.theme = new Theme(jar.getInputStream(entry));
				} catch (IOException e) {
					throw new ThemeException("Failed to read description: "
							+ this.file + ":" + this.path
							+ Theme.DESCRIPTIONFILE);
				}
			}

			Collection fileNames = theme.getFileNames(type, Theme.MODE_HTML);
			// Add all XSL file streams
			for (Iterator i = fileNames.iterator(); i.hasNext();) {
				entry = jar.getJarEntry(this.path + (String) i.next());
				if (entry.getName().endsWith(".xsl"))
					try {
						xslFiles.add(new XSLStream(entry.getName(), jar
								.getInputStream(entry)));
					} catch (java.io.FileNotFoundException e) {
						throw new ThemeException("XSL File not found: "
								+ this.file + ": " + entry);
					} catch (java.io.IOException e) {
						throw new ThemeException("Failed to read XSL file. "
								+ this.file + ": " + entry);
					}
			}

		} else {

			// Handle subdirectories in archive: return the first match
			for (Iterator i = this.subdirs.iterator(); i.hasNext();) {
				ThemeSource source = (ThemeSource) i.next();
				if (source.getThemes().contains(theme))
					xslFiles.addAll(source.getXSLStreams(theme, type));
			}
		}

		return xslFiles;
	}

	/**
	 * Returns modication time of the jar file.
	 * 
	 * @see com.itmill.toolkit.terminal.web.ThemeSource#getModificationTime()
	 */
	public long getModificationTime() {
		return this.file.lastModified();
	}

	/**
	 * Gets the input stream for the resource with the specified resource id.
	 * @see com.itmill.toolkit.terminal.web.ThemeSource#getResource(String)
	 */
	public InputStream getResource(String resourceId)
			throws ThemeSource.ThemeException {

		// Strip off the theme name prefix from resource id
		if (this.theme != null && this.theme.getName() != null
				&& resourceId.startsWith(this.theme.getName() + "/")) {
			resourceId = resourceId
					.substring(this.theme.getName().length() + 1);
		}

		// Returns the resource inside the jar file
		JarEntry entry = jar.getJarEntry(resourceId);
		if (entry != null)
			try {

				// Try cache
				byte[] data = (byte[]) resourceCache.get(entry);
				if (data != null)
					return new ByteArrayInputStream(data);

				// Reads data
				int bufSize = 1024;
				ByteArrayOutputStream out = new ByteArrayOutputStream(bufSize);
				InputStream in = jar.getInputStream(entry);
				byte[] buf = new byte[bufSize];
				int n = 0;
				while ((n = in.read(buf)) >= 0) {
					out.write(buf, 0, n);
				}
				in.close();
				data = out.toByteArray();

				// Cache data
				resourceCache.put(entry, data);
				return new ByteArrayInputStream(data);
			} catch (IOException e) {
			}

		throw new ThemeSource.ThemeException("Resource " + resourceId
				+ " not found.");
	}

	/**
	 * Gets the list of themes in the theme source.
	 * @see com.itmill.toolkit.terminal.web.ThemeSource#getThemes()
	 */
	public Collection getThemes() {
		Collection themes = new LinkedList();
		if (this.theme != null) {
			themes.add(this.theme);
		} else {
			for (Iterator i = this.subdirs.iterator(); i.hasNext();) {
				ThemeSource source = (ThemeSource) i.next();
				themes.addAll(source.getThemes());
			}
		}
		return themes;
	}

	/**
	 * Gets the name of the ThemeSource.
	 * @see com.itmill.toolkit.terminal.web.ThemeSource#getName()
	 */
	public String getName() {
		if (this.theme != null) {
			return this.theme.getName();
		} else {
			return this.name;
		}
	}

	/**
	 * Gets the Theme instance by name.
	 * @see com.itmill.toolkit.terminal.web.ThemeSource#getThemeByName(String)
	 */
	public Theme getThemeByName(String name) {
		Collection themes = this.getThemes();
		for (Iterator i = themes.iterator(); i.hasNext();) {
			Theme t = (Theme) i.next();
			if (name != null && name.equals(t.getName()))
				return t;
		}
		return null;
	}

	/**
	 * @author IT Mill Ltd.
	 * @version
	 * @VERSION@
	 * @since 3.0
	 */
	private class Cache {

		private Map data = new HashMap();
		
		/**
		 * Associates the specified value with the specified key in this map.
		 * If the map previously contained a mapping for this key, the old value
		 * is replaced by the specified value.
		 * @param key the key with which the specified value is to be associated.
		 * @param value the value to be associated with the specified key. 
		 */
		public void put(Object key, Object value) {
			data.put(key, new SoftReference(new CacheItem(value)));
		}
		
		/**
		 * Returns the value to which this map maps the specified key. Returns null
		 * if the map contains no mapping for this key. 
		 * <p>
		 * A return value of null does not necessarily indicate that the map contains
		 * no mapping for the key; it's also possible that the map explicitly maps 
		 * the key to null. The containsKey operation may be used to distinguish these two cases. 
		 * </p>
		 * @param key the key whose associated value is to be returned. 
		 * @return the value to which this map maps the specified key, or null
		 *  		if the map contains no mapping for this key.
		 */
		public Object get(Object key) {
			SoftReference ref = (SoftReference) data.get(key);
			if (ref != null)
				return ((CacheItem) ref.get()).getData();
			return null;
		}
		
		/**
		 * Clears the data.
		 *
		 */
		public void clear() {
			data.clear();
		}
	}

	/**
	 * @author IT Mill Ltd.
	 * @version
	 * @VERSION@
	 * @since 3.0
	 */
	private class CacheItem {

		private Object data;
		
		/**
		 * 
		 * @param data
		 */
		public CacheItem(Object data) {
			this.data = data;
		}
		
		/**
		 * 
		 * @return
		 */
		public Object getData() {
			return this.data;
		};
		
		/**
		 * @see java.lang.Object#finalize()
		 */
		public void finalize() throws Throwable {
			this.data = null;
			super.finalize();
		}

	}

}
