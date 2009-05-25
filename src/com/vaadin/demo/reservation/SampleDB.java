/* 
@ITMillApache2LicenseForJavaFiles@
 */

package com.vaadin.demo.reservation;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.QueryContainer;

public class SampleDB {
    public class User {
        public static final String TABLE = "USER";
        public static final String PROPERTY_ID_ID = TABLE + "_ID";
        public static final String PROPERTY_ID_FULLNAME = TABLE + "_FULLNAME";
        public static final String PROPERTY_ID_EMAIL = TABLE + "_EMAIL";
        public static final String PROPERTY_ID_PASSWORD = TABLE + "_PASSWORD";
        public static final String PROPERTY_ID_DELETED = TABLE + "_DELETED";
    }

    public class Resource {
        public static final String TABLE = "RESOURCE";
        public static final String PROPERTY_ID_ID = TABLE + "_ID";
        public static final String PROPERTY_ID_STYLENAME = TABLE + "_STYLENAME";
        public static final String PROPERTY_ID_NAME = TABLE + "_NAME";
        public static final String PROPERTY_ID_DESCRIPTION = TABLE
                + "_DESCRIPTION";
        public static final String PROPERTY_ID_LOCATIONX = TABLE
                + "_LOCATION_X";
        public static final String PROPERTY_ID_LOCATIONY = TABLE
                + "_LOCATION_Y";
        public static final String PROPERTY_ID_CATEGORY = TABLE + "_CATEGORY";
        public static final String PROPERTY_ID_DELETED = TABLE + "_DELETED";
    }

    public class Reservation {
        public static final String TABLE = "RESERVATION";
        public static final String PROPERTY_ID_ID = TABLE + "_ID";
        public static final String PROPERTY_ID_DESCRIPTION = TABLE
                + "_DESCRIPTION";
        public static final String PROPERTY_ID_RESOURCE_ID = TABLE
                + "_RESOURCE_ID";
        public static final String PROPERTY_ID_RESERVED_BY_ID = TABLE
                + "_RESERVED_BY_USER_ID";
        public static final String PROPERTY_ID_RESERVED_FROM = TABLE
                + "_RESERVED_FROM";
        public static final String PROPERTY_ID_RESERVED_TO = TABLE
                + "_RESERVED_TO";
    }

    private static final String DEFAULT_JDBC_URL = "jdbc:hsqldb:file:reservation.db";
    private static Object dbMutex = new Object();

    private static final String CREATE_TABLE_USER = "CREATE TABLE "
            + User.TABLE + " (" + " " + User.PROPERTY_ID_ID
            + " INTEGER IDENTITY" + ", " + User.PROPERTY_ID_FULLNAME
            + " VARCHAR(100) NOT NULL" + ", " + User.PROPERTY_ID_EMAIL
            + " VARCHAR(50) NOT NULL" + ", " + User.PROPERTY_ID_PASSWORD
            + " VARCHAR(20) NOT NULL" + ", " + User.PROPERTY_ID_DELETED
            + " BOOLEAN DEFAULT false NOT NULL" + ", UNIQUE("
            + User.PROPERTY_ID_FULLNAME + "), UNIQUE(" + User.PROPERTY_ID_EMAIL
            + ") )";
    private static final String CREATE_TABLE_RESOURCE = "CREATE TABLE "
            + Resource.TABLE + " (" + " " + Resource.PROPERTY_ID_ID
            + " INTEGER IDENTITY" + ", " + Resource.PROPERTY_ID_STYLENAME
            + " VARCHAR(20) NOT NULL" + ", " + Resource.PROPERTY_ID_NAME
            + " VARCHAR(30) NOT NULL" + ", " + Resource.PROPERTY_ID_DESCRIPTION
            + " VARCHAR(100)" + ", " + Resource.PROPERTY_ID_LOCATIONX
            + " DOUBLE" + ", " + Resource.PROPERTY_ID_LOCATIONY + " DOUBLE"
            + ", " + Resource.PROPERTY_ID_CATEGORY + " VARCHAR(30)" + ", "
            + Resource.PROPERTY_ID_DELETED + " BOOLEAN DEFAULT false NOT NULL"
            + ", UNIQUE(" + Resource.PROPERTY_ID_NAME + "))";
    private static final String CREATE_TABLE_RESERVATION = "CREATE TABLE "
            + Reservation.TABLE + " (" + " " + Reservation.PROPERTY_ID_ID
            + " INTEGER IDENTITY" + ", " + Reservation.PROPERTY_ID_RESOURCE_ID
            + " INTEGER" + ", " + Reservation.PROPERTY_ID_RESERVED_BY_ID
            + " INTEGER" + ", " + Reservation.PROPERTY_ID_RESERVED_FROM
            + " TIMESTAMP NOT NULL" + ", "
            + Reservation.PROPERTY_ID_RESERVED_TO + " TIMESTAMP NOT NULL"
            + ", " + Reservation.PROPERTY_ID_DESCRIPTION + " VARCHAR(100)"
            + ", FOREIGN KEY (" + Reservation.PROPERTY_ID_RESOURCE_ID
            + ") REFERENCES " + Resource.TABLE + "(" + Resource.PROPERTY_ID_ID
            + "), FOREIGN KEY (" + Reservation.PROPERTY_ID_RESERVED_BY_ID
            + ") REFERENCES " + User.TABLE + "(" + User.PROPERTY_ID_ID + "))";

    private static Connection connection = null;

    /**
     * Create database.
     * 
     * @param jdbcUrl
     */
    public SampleDB(String jdbcUrl) {
        if (jdbcUrl == null || jdbcUrl.equals("")) {
            jdbcUrl = DEFAULT_JDBC_URL;
        }
        // connect to SQL database
        connect(jdbcUrl);

    }

    private synchronized void dropTables() {
        try {
            update("DROP TABLE " + Reservation.TABLE);
        } catch (final SQLException IGNORED) {
            // IGNORED, assuming it was not there
        }
        try {
            update("DROP TABLE " + Resource.TABLE);
        } catch (final SQLException IGNORED) {
            // IGNORED, assuming it was not there
        }
        try {
            update("DROP TABLE " + User.TABLE);
        } catch (final SQLException IGNORED) {
            // IGNORED, assuming it was not there
        }
    }

    /**
     * Connect to SQL database. In this sample we use HSQLDB and an toolkit
     * named database in implicitly created into system memory.
     * 
     */
    private synchronized void connect(String dbUrl) {
        if (connection == null) {
            try {
                Class.forName("org.hsqldb.jdbcDriver").newInstance();
                connection = DriverManager.getConnection(dbUrl);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }

            dropTables();
            // initialize SQL database
            createTables();

            // test by executing sample JDBC query
            testDatabase();

            generateResources();
            generateDemoUser();
            generateReservations();

        }
    }

    /**
     * use for SQL commands CREATE, DROP, INSERT and UPDATE
     * 
     * @param expression
     * @throws SQLException
     */
    private synchronized void update(String expression) throws SQLException {
        Statement st = null;
        st = connection.createStatement();
        final int i = st.executeUpdate(expression);
        if (i == -1) {
            System.out.println("SampleDatabase error : " + expression);
        }
        st.close();
    }

    /**
     * Create test table and few rows. Issue note: using capitalized column
     * names as HSQLDB returns column names in capitalized form with this demo.
     * 
     */
    private synchronized void createTables() {
        try {
            String stmt = null;
            stmt = CREATE_TABLE_RESOURCE;
            update(stmt);
        } catch (final SQLException e) {
            if (e.toString().indexOf("Table already exists") == -1) {
                throw new RuntimeException(e);
            }
        }
        try {
            String stmt = null;
            stmt = CREATE_TABLE_USER;
            update(stmt);
        } catch (final SQLException e) {
            if (e.toString().indexOf("Table already exists") == -1) {
                throw new RuntimeException(e);
            }
        }
        try {
            String stmt = null;
            stmt = CREATE_TABLE_RESERVATION;
            update(stmt);
        } catch (final SQLException e) {
            if (e.toString().indexOf("Table already exists") == -1) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Test database connection with simple SELECT command.
     * 
     */
    private synchronized String testDatabase() {
        String result = null;
        try {
            final Statement stmt = connection.createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            final ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM "
                    + Resource.TABLE);
            rs.next();
            result = "rowcount for table test is " + rs.getObject(1).toString();
            stmt.close();
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public Connection getConnection() {
        return connection;
    }

    public Container getCategories() {
        // TODO where deleted=?
        final String q = "SELECT DISTINCT(" + Resource.PROPERTY_ID_CATEGORY
                + ") FROM " + Resource.TABLE + " ORDER BY "
                + Resource.PROPERTY_ID_CATEGORY;
        try {
            return new QueryContainer(q, connection,
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public Container getResources(String category) {
        // TODO where deleted=?
        String q = "SELECT * FROM " + Resource.TABLE;
        if (category != null) {
            q += " WHERE " + Resource.PROPERTY_ID_CATEGORY + "='" + category
                    + "'"; // FIXME ->
            // PreparedStatement!
        }

        try {
            return new QueryContainer(q, connection,
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public Container getReservations(List<?> resources) {
        // TODO where reserved_by=?
        // TODO where from=?
        // TODO where to=?
        // TODO where deleted=?
        String q = "SELECT * FROM " + Reservation.TABLE + "," + Resource.TABLE;
        q += " WHERE " + Reservation.PROPERTY_ID_RESOURCE_ID + "="
                + Resource.PROPERTY_ID_ID;
        if (resources != null && resources.size() > 0) {
            final StringBuffer s = new StringBuffer();
            for (final Iterator<?> it = resources.iterator(); it.hasNext();) {
                if (s.length() > 0) {
                    s.append(",");
                }
                s.append(((Item) it.next())
                        .getItemProperty(Resource.PROPERTY_ID_ID));
            }
            q += " HAVING " + Reservation.PROPERTY_ID_RESOURCE_ID + " IN (" + s
                    + ")";
        }
        q += " ORDER BY " + Reservation.PROPERTY_ID_RESERVED_FROM;
        try {
            final QueryContainer qc = new QueryContainer(q, connection,
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            if (qc.size() < 1) {
                return null;
            } else {
                return qc;
            }
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void addReservation(Item resource, int reservedById,
            Date reservedFrom, Date reservedTo, String description) {
        if (reservedFrom.after(reservedTo)) {
            final Date tmp = reservedTo;
            reservedTo = reservedFrom;
            reservedFrom = tmp;
        }
        final int resourceId = ((Integer) resource.getItemProperty(
                Resource.PROPERTY_ID_ID).getValue()).intValue();
        final String q = "INSERT INTO " + Reservation.TABLE + " ("
                + Reservation.PROPERTY_ID_RESOURCE_ID + ","
                + Reservation.PROPERTY_ID_RESERVED_BY_ID + ","
                + Reservation.PROPERTY_ID_RESERVED_FROM + ","
                + Reservation.PROPERTY_ID_RESERVED_TO + ","
                + Reservation.PROPERTY_ID_DESCRIPTION + ")"
                + "VALUES (?,?,?,?,?)";
        synchronized (dbMutex) {
            try {
                if (!isAvailableResource(resourceId, reservedFrom, reservedTo)) {
                    throw new ResourceNotAvailableException(
                            "The resource is not available at that time.");
                }
                final PreparedStatement p = connection.prepareStatement(q);
                p.setInt(1, resourceId);
                p.setInt(2, reservedById);
                p.setTimestamp(3,
                        new java.sql.Timestamp(reservedFrom.getTime()));
                p.setTimestamp(4, new java.sql.Timestamp(reservedTo.getTime()));
                p.setString(5, description);
                p.execute();
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean isAvailableResource(int resourceId, Date reservedFrom,
            Date reservedTo) {
        // TODO where deleted=?
        if (reservedFrom.after(reservedTo)) {
            final Date tmp = reservedTo;
            reservedTo = reservedFrom;
            reservedFrom = tmp;
        }
        final String checkQ = "SELECT count(*) FROM " + Reservation.TABLE
                + " WHERE " + Reservation.PROPERTY_ID_RESOURCE_ID + "=? AND (("
                + Reservation.PROPERTY_ID_RESERVED_FROM + ">=? AND "
                + Reservation.PROPERTY_ID_RESERVED_FROM + "<?) OR ("
                + Reservation.PROPERTY_ID_RESERVED_TO + ">? AND "
                + Reservation.PROPERTY_ID_RESERVED_TO + "<=?) OR ("
                + Reservation.PROPERTY_ID_RESERVED_FROM + "<=? AND "
                + Reservation.PROPERTY_ID_RESERVED_TO + ">=?)" + ")";
        try {
            final PreparedStatement p = connection.prepareStatement(checkQ);
            p.setInt(1, resourceId);
            p.setTimestamp(2, new java.sql.Timestamp(reservedFrom.getTime()));
            p.setTimestamp(3, new java.sql.Timestamp(reservedTo.getTime()));
            p.setTimestamp(4, new java.sql.Timestamp(reservedFrom.getTime()));
            p.setTimestamp(5, new java.sql.Timestamp(reservedTo.getTime()));
            p.setTimestamp(6, new java.sql.Timestamp(reservedFrom.getTime()));
            p.setTimestamp(7, new java.sql.Timestamp(reservedTo.getTime()));
            p.execute();
            final ResultSet rs = p.getResultSet();
            if (rs.next() && rs.getInt(1) > 0) {
                return false;
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public Container getUsers() {
        // TODO where deleted=?
        final String q = "SELECT * FROM " + User.TABLE + " ORDER BY "
                + User.PROPERTY_ID_FULLNAME;
        try {
            final QueryContainer qc = new QueryContainer(q, connection,
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            return qc;
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("deprecation")
    public synchronized void generateReservations() {
        final int days = 30;
        final String descriptions[] = { "Picking up guests from airport",
                "Sightseeing with the guests",
                "Moving new servers from A to B", "Shopping",
                "Customer meeting", "Guests arriving at harbour",
                "Moving furniture", "Taking guests to see town" };
        final Container cat = getCategories();
        final Collection<?> cIds = cat.getItemIds();
        for (final Iterator<?> it = cIds.iterator(); it.hasNext();) {
            final Object id = it.next();
            final Item ci = cat.getItem(id);
            final String c = (String) ci.getItemProperty(
                    Resource.PROPERTY_ID_CATEGORY).getValue();
            final Container resources = getResources(c);
            final Collection<?> rIds = resources.getItemIds();
            final Calendar cal = Calendar.getInstance();
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            final int hourNow = new Date().getHours();
            // cal.add(Calendar.DAY_OF_MONTH, -days);
            for (int i = 0; i < days; i++) {
                int r = 3;
                for (final Iterator<?> rit = rIds.iterator(); rit.hasNext()
                        && r > 0; r--) {
                    final Object rid = rit.next();
                    final Item resource = resources.getItem(rid);
                    final int s = hourNow - 6
                            + (int) Math.round(Math.random() * 6.0);
                    final int e = s + 1 + (int) Math.round(Math.random() * 4.0);
                    final Date start = new Date(cal.getTimeInMillis());
                    start.setHours(s);
                    final Date end = new Date(cal.getTimeInMillis());
                    end.setHours(e);
                    addReservation(resource, 0, start, end,
                            descriptions[(int) Math.floor(Math.random()
                                    * descriptions.length)]);
                }
                cal.add(Calendar.DATE, 1);
            }
        }

    }

    public synchronized void generateResources() {

        final Object[][] resources = {
                // Turku
                { "01", "01 Ford Mondeo", "w/ company logo", "Turku",
                        new Double(60.510857), new Double(22.275424) },
                { "02", "02 Citroen Jumper",
                        "w/ company logo. 12m3 storage space.", "Turku",
                        new Double(60.452171), new Double(22.2995) },
                { "03", "03 Saab 93", "Cabriolet. Keys from the rental desk.",
                        "Turku", new Double(60.4507), new Double(22.295551) },
                { "04", "04 Volvo S60", "Key from the rental desk.", "Turku",
                        new Double(60.434722), new Double(22.224398) },
                { "05", "05 Smart fourtwo", "Cabrio. Keys from infodesk.",
                        "Turku", new Double(60.508970), new Double(22.264790) },
                // Helsinki
                { "06", "06 Smart fourtwo", "Cabrio. Keys from infodesk.",
                        "Helsinki", new Double(60.17175), new Double(24.939029) },
                { "07", "07 Smart fourtwo", "Cabrio. Keys from infodesk.",
                        "Helsinki", new Double(60.17175), new Double(24.939029) },
                { "08", "08 Smart fourtwo", "Cabrio. Keys from infodesk.",
                        "Helsinki", new Double(60.166579),
                        new Double(24.953899) },
                { "09", "09 Volvo S60", "Keys from infodesk.", "Helsinki",
                        new Double(60.317832), new Double(24.967289) },
                { "10", "10 Saab 93", "Keys from infodesk.", "Helsinki",
                        new Double(60.249193), new Double(25.045921) },
                // Silicon Valley
                { "11", "11 Ford Mustang", "Keys from Acme clerk.",
                        "Silicon Valley", new Double(37.615853),
                        new Double(-122.386384) },
                { "12", "12 Ford Fusion", "Keys from infodesk.",
                        "Silicon Valley", new Double(37.365028),
                        new Double(-121.922654) },
                { "13", "13 Land Rover", "Keys from infodesk.",
                        "Silicon Valley", new Double(37.365028),
                        new Double(-121.922654) },
                { "14", "14 Land Rover", "Keys from infodesk.",
                        "Silicon Valley", new Double(37.365028),
                        new Double(-121.922654) },
                { "15", "15 Ford Mustang", "GT Cal Special. Keys from guard.",
                        "Silicon Valley", new Double(37.403812),
                        new Double(-121.977425) },
                { "16", "16 Ford Focus", "Keys from guard.", "Silicon Valley",
                        new Double(37.403812), new Double(-121.977425) },
                // Paris
                { "17", "17 Peugeot 308", "Keys from infodesk.", "Paris",
                        new Double(48.844756), new Double(2.372784) },
                { "18", "18 Citroen C6", "Keys from rental desk.", "Paris",
                        new Double(49.007253), new Double(2.545025) },
                { "19", "19 Citroen C6", "Keys from infodesk.", "Paris",
                        new Double(48.729061), new Double(2.368087) },
                { "20", "20 Peugeot 308", "Keys from ticket sales.", "Paris",
                        new Double(48.880931), new Double(2.356988) },
                { "21", "21 Peugeot 308", "Keys from ticket sales.", "Paris",
                        new Double(48.876479), new Double(2.358161) },
                // STHLM
                { "22", "22 Volvo S60", "Keys from infodesk.", "Stockholm",
                        new Double(59.350414), new Double(18.106574) },
                { "23", "23 Saab 93", "Keys from infodesk.", "Stockholm",
                        new Double(59.355905), new Double(17.946784) },
                { "24", "24 Smart fourtwo", "Keys from infodesk.", "Stockholm",
                        new Double(59.315939), new Double(18.095904) },
                { "25", "25 Smart fourtwo", "Keys from infodesk.", "Stockholm",
                        new Double(59.330716), new Double(18.058702) },
        // Boston
        /*
         * { "26", "26 Ford Mustang", "Keys from infodesk.", "Boston", new
         * Double(42.366588), new Double(-71.020955) }, { "27", "27 Smart
         * fourtwo", "Keys from infodesk.", "Boston", new Double(42.365419), new
         * Double(-71.061748) }, { "28", "28 Volvo S60", "Keys from Seaport
         * Hotel reception.", "Boston", new Double(42.34811), new
         * Double(-71.041127) }, { "29", "29 Smart fourtwo", "Keys from Seaport
         * Hotel reception.", "Boston", new Double(42.348072), new
         * Double(-71.041315) },
         */

        };

        final String q = "INSERT INTO " + Resource.TABLE + "("
                + Resource.PROPERTY_ID_STYLENAME + ","
                + Resource.PROPERTY_ID_NAME + ","
                + Resource.PROPERTY_ID_DESCRIPTION + ","
                + Resource.PROPERTY_ID_CATEGORY + ","
                + Resource.PROPERTY_ID_LOCATIONX + ","
                + Resource.PROPERTY_ID_LOCATIONY + ")"
                + " VALUES (?,?,?,?,?,?)";
        try {
            final PreparedStatement stmt = connection.prepareStatement(q);
            for (int i = 0; i < resources.length; i++) {
                int j = 0;
                stmt.setString(j + 1, (String) resources[i][j++]);
                stmt.setString(j + 1, (String) resources[i][j++]);
                stmt.setString(j + 1, (String) resources[i][j++]);
                stmt.setString(j + 1, (String) resources[i][j++]);
                stmt.setDouble(j + 1, ((Double) resources[i][j++])
                        .doubleValue());
                stmt.setDouble(j + 1, ((Double) resources[i][j++])
                        .doubleValue());
                stmt.execute();
            }
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void generateDemoUser() {
        final String q = "INSERT INTO USER (" + User.PROPERTY_ID_FULLNAME + ","
                + User.PROPERTY_ID_EMAIL + "," + User.PROPERTY_ID_PASSWORD
                + ") VALUES (?,?,?)";
        try {
            final PreparedStatement stmt = connection.prepareStatement(q);
            stmt.setString(1, "Demo User");
            stmt.setString(2, "demo.user@vaadin.com");
            stmt.setString(3, "demo");
            stmt.execute();
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }

    }

}
