package de.fhg.iais.roberta.javaServer.basics;

import java.util.List;

import org.hibernate.Session;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.fhg.iais.roberta.persistence.bo.Program;
import de.fhg.iais.roberta.persistence.bo.Relation;
import de.fhg.iais.roberta.persistence.bo.Robot;
import de.fhg.iais.roberta.persistence.bo.Role;
import de.fhg.iais.roberta.persistence.bo.User;
import de.fhg.iais.roberta.persistence.bo.UserProgramShare;
import de.fhg.iais.roberta.persistence.dao.AccessRightDao;
import de.fhg.iais.roberta.persistence.dao.ProgramDao;
import de.fhg.iais.roberta.persistence.dao.RobotDao;
import de.fhg.iais.roberta.persistence.dao.UserDao;
import de.fhg.iais.roberta.persistence.util.DbSession;
import de.fhg.iais.roberta.persistence.util.DbSetup;
import de.fhg.iais.roberta.persistence.util.SessionFactoryWrapper;

public class PersistUserProgramTest {
    private SessionFactoryWrapper sessionFactoryWrapper;
    private DbSetup memoryDbSetup;
    private String connectionUrl;

    private static final int TOTAL_USERS = 100;

    @Before
    public void setup() throws Exception {
        this.connectionUrl = "jdbc:hsqldb:mem:userProgramInMemoryDb";
        this.sessionFactoryWrapper = new SessionFactoryWrapper("hibernate-test-cfg.xml", this.connectionUrl);
        Session nativeSession = this.sessionFactoryWrapper.getNativeSession();
        this.memoryDbSetup = new DbSetup(nativeSession);
        this.memoryDbSetup.createEmptyDatabase();
    }

    @Test
    public void test() throws Exception {
        DbSession hSession = this.sessionFactoryWrapper.getSession();
        UserDao userDao = new UserDao(hSession);
        RobotDao robotDao = new RobotDao(hSession);
        ProgramDao programDao = new ProgramDao(hSession);
        Robot robot = robotDao.loadRobot("ev3");
        Assert.assertEquals(0, getOneBigInteger("select count(*) from USER_PROGRAM"));

        //Create list of users
        for ( int userNumber = 0; userNumber < PersistUserProgramTest.TOTAL_USERS; userNumber++ ) {
            User user = userDao.loadUser("account-" + userNumber);
            if ( user == null ) {
                User user2 = new User(null, "account-" + userNumber);
                user2.setEmail("stuff");
                user2.setPassword("pass-" + userNumber);
                user2.setRole(Role.STUDENT);
                user2.setTags("rwth");
                hSession.save(user2);
                hSession.commit();
            }
        }
        List<User> userList = userDao.loadUserList("created", 0, "rwth");
        Assert.assertTrue(userList.size() == 10);

        //Create one program per user
        for ( int userNumber = 0; userNumber < PersistUserProgramTest.TOTAL_USERS; userNumber++ ) {
            User owner = userDao.loadUser("account-" + userNumber);
            Program program = programDao.load("program-" + userNumber, owner, robot, owner);
            if ( program == null ) {
                Program program2 = new Program("program-" + userNumber, owner, robot, owner);
                String text = "<program>...</program>";
                program2.setProgramText(text);
                hSession.save(program2);
                hSession.commit();
            }
        }
        List<Program> programList = programDao.loadAll();
        Assert.assertTrue(programList.size() == 101);

        //User 0 invites all inpair  users to write to its program
        User owner = userDao.loadUser("account-0");
        Program program = programDao.load("program-0", owner, robot, owner);
        AccessRightDao userProgramDao = new AccessRightDao(hSession);
        for ( int userNumber = 1; userNumber < PersistUserProgramTest.TOTAL_USERS; userNumber += 2 ) {
            User user = userDao.loadUser("account-" + userNumber);
            if ( user != null ) {
                UserProgramShare userProgram = userProgramDao.loadAccessRight(user, program);
                if ( userProgram == null ) {
                    UserProgramShare userProgram2 = new UserProgramShare(user, program, Relation.WRITE);
                    hSession.save(userProgram2);
                    hSession.commit();
                }
            }
        }

        //Show list of users from program dao
        List<UserProgramShare> userProgramList = userProgramDao.loadAccessRightsByProgram(program);
        Assert.assertTrue(userProgramList.size() == 50);
        for ( int userNumber = 1; userNumber < PersistUserProgramTest.TOTAL_USERS; userNumber += 2 ) {
            User user = userDao.loadUser("account-" + userNumber);
            List<UserProgramShare> userProgramList2 = userProgramDao.loadAccessRightsForUser(user, robot);
            Assert.assertTrue(userProgramList2.size() == 1);
        }
    }

    @After
    public void tearDown() {
        this.memoryDbSetup.deleteAllFromUserAndProgramTmpPasswords();
    }

    private long getOneBigInteger(String sqlStmt) {
        return this.memoryDbSetup.getOneBigIntegerAsLong(sqlStmt);
    }
}
