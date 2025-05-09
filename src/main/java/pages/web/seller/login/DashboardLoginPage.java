package pages.web.seller.login;

import api.seller.login.APISellerLogin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.pagefactory.ByChained;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import utility.PropertiesUtils;
import utility.WebUtils;

/**
 * Represents the login page of the seller's dashboard.
 * This class provides functionality to log in to the dashboard by setting the necessary authentication tokens and
 * user information in local storage using JavaScript.
 */
public class DashboardLoginPage {

    private static final Logger logger = LogManager.getLogger();

    private final WebDriver driver;
    private final WebUtils webUtils;
    private String langKey;

    /**
     * Constructs a new instance of `DashboardLoginPage`.
     *
     * @param driver The WebDriver instance used to interact with the browser.
     */
    public DashboardLoginPage(WebDriver driver) {
        this.driver = driver;
        this.webUtils = new WebUtils(driver);
    }

    // Locators for login page elements (could be moved to a separate file for better maintainability)
    By loc_frmLogin = By.xpath("//div[contains(@class,'login-widget__formBody') and not(@hidden)]");
    By loc_txtUsername = new ByChained(loc_frmLogin, By.cssSelector("input[name='username']"));
    By loc_txtPassword = new ByChained(loc_frmLogin, By.cssSelector("input[name='password']"));
    By loc_btnLogin = new ByChained(loc_frmLogin, By.xpath(".//button[@type='submit']"));

    By loc_lblUsernameError = By.cssSelector("#username + .invalid-feedback");
    By loc_lblPasswordError = By.cssSelector("#password + .invalid-feedback");
    By loc_lblLoginFailError = By.cssSelector("div[class~='alert__wrapper']:not(div[hidden])");

    /**
     * Navigates to the login page of the seller's dashboard.
     * This method opens the login page URL.
     *
     * @return The current instance of `DashboardLoginPage`.
     */
    public DashboardLoginPage navigateToLoginPage() {
        driver.get(PropertiesUtils.getDomain() + "/login");
        return this;
    }

    /**
     * Selects the display language on the login page.
     * This method retrieves the current language from local storage and updates it if needed.
     *
     * @return The current instance of `DashboardLoginPage`.
     */
    public DashboardLoginPage selectDisplayLanguage() {
        if (PropertiesUtils.getEnv().contains("BIZ")) {
            this.langKey = "en";
            logger.info("Shop BIZ does not have language to select.");
            return this;
        }

        this.langKey = PropertiesUtils.getLangKey();
        webUtils.setLocalStorageValue("langKey", langKey);
        logger.info("Selected display language '{}'.", this.langKey);

        return this;
    }

    /**
     * Inputs the given username into the username field.
     *
     * @param username The username to input.
     * @return The current instance of `DashboardLoginPage`.
     */
    public DashboardLoginPage inputUsername(String username) {
        // Must input then clear to get error message
        if (username.isEmpty()) {
            webUtils.sendKeys(loc_txtUsername, "a");
            webUtils.sendKeys(loc_txtUsername, "");
            return this;
        }

        webUtils.sendKeys(loc_txtUsername, username);
        logger.info("Input '****' into Username field.");
        return this;
    }

    /**
     * Inputs the given password into the password field.
     *
     * @param password The password to input.
     * @return The current instance of `DashboardLoginPage`.
     */
    public DashboardLoginPage inputPassword(String password) {
        // Must input then clear to get error message
        if (password.isEmpty()) {
            webUtils.sendKeys(loc_txtPassword, "a");
            webUtils.sendKeys(loc_txtPassword, "");
            return this;
        }

        webUtils.sendKeys(loc_txtPassword, password);
        logger.info("Input '****' into Password field.");
        return this;
    }

    /**
     * Clicks on the login button.
     *
     * @return The current instance of `DashboardLoginPage`.
     */
    public DashboardLoginPage clickLoginBtn() {
        webUtils.click(loc_btnLogin);
        logger.info("Clicked on Login button.");
        return this;
    }

    /**
     * Performs the login process by inputting the username, password, and clicking the login button.
     * Waits for the dot spinner to disappear after clicking the login button.
     *
     * @param username The username to input.
     * @param password The password to input.
     */
    private void performLogin(String username, String password) {
        inputUsername(username);
        inputPassword(password);
        clickLoginBtn();
    }

    /**
     * Performs a valid login to the dashboard by providing the username and password.
     *
     * @param username The username to input.
     * @param password The password to input.
     * @return The current instance of `DashboardLoginPage`.
     */
    public DashboardLoginPage performValidLogin(String username, String password) {
        WebUtils.performAction("Login to dashboard",
                () -> performLogin(username, password),
                this::verifyLoginWithCorrectAccount);
        return this;
    }

    /**
     * Verifies that the appropriate error messages are displayed when the username and password fields are left blank.
     */
    public void verifyErrorWhenLeaveAllBlank() {
        logger.info("Verifying error messages when username and password fields are left blank.");

        String usernameErr = getUsernameError();
        Assert.assertEquals(usernameErr, PropertiesUtils.getDashboardProperty("input.blank.error", langKey), "Username blank error not match.");

        String passwordErr = getPasswordError();
        Assert.assertEquals(passwordErr, PropertiesUtils.getDashboardProperty("input.blank.error", langKey), "Password blank error not match.");
    }

    /**
     * Verifies that an error message is displayed when an invalid phone format is entered.
     */
    public void verifyWhenErrorInputInvalidPhoneFormat() {
        logger.info("Verifying error message when an invalid phone format is entered.");

        String usernameErr = getUsernameError();
        Assert.assertEquals(usernameErr, PropertiesUtils.getDashboardProperty("login.screen.error.invalidPhone", langKey), "Invalid phone format error not match.");
    }

    /**
     * Verifies that an error message is displayed when an invalid email format is entered.
     */
    public void verifyErrorWhenInputInvalidMailFormat() {
        logger.info("Verifying error message when an invalid email format is entered.");

        String usernameErr = getUsernameError();
        Assert.assertEquals(usernameErr, PropertiesUtils.getDashboardProperty("login.screen.error.invalidMail", langKey), "Invalid mail format error not match.");
    }

    /**
     * Verifies that the login fails with an error message when non-existent account credentials are used.
     */
    public void verifyLoginFailErrorWhenLoginNonExistAccount() {
        logger.info("Verifying login failure error message when non-existent account credentials are used.");

        String loginFailErr = getLoginFailError();
        Assert.assertEquals(loginFailErr, PropertiesUtils.getDashboardProperty("login.screen.error.wrongCredentials", langKey), "Login fail error not match.");
    }

    /**
     * Verifies that after logging in with valid credentials, the user is redirected to the home page.
     * <p>
     * This method waits for up to 3 seconds to ensure the URL contains the "/home" path, indicating
     * that the user has successfully navigated to the home page. If the URL does not contain "/home"
     * within the specified time, an exception is thrown indicating a failure to navigate to the home screen.
     * </p>
     */
    public void verifyLoginWithCorrectAccount() {
        try {
            logger.info("Verifying that the user is redirected to the home page after login.");
            webUtils.waitForCondition(ExpectedConditions.urlContains("/home"), 30_000);
        } catch (TimeoutException exception) {
            throw new RuntimeException("Cannot navigate to home screen", exception);
        }
    }


    /**
     * Retrieves the login failure error message.
     *
     * @return The error message displayed when login fails.
     */
    public String getLoginFailError() {
        String text = webUtils.getText(loc_lblLoginFailError);
        logger.info("Login fail error retrieved: {}", text);
        return text;
    }

    /**
     * Retrieves the error message displayed next to the username field.
     *
     * @return The error message for the username.
     */
    public String getUsernameError() {
        String text = webUtils.getText(loc_lblUsernameError);
        logger.info("Username error retrieved: {}", text);
        return text;
    }

    /**
     * Retrieves the error message displayed next to the password field.
     *
     * @return The error message for the password.
     */
    public String getPasswordError() {
        String text = webUtils.getText(loc_lblPasswordError);
        logger.info("Password error retrieved: {}", text);
        return text;
    }

    /**
     * Logs in to the dashboard by setting local storage items using JavaScript.
     * This method accesses the dashboard URL, retrieves login credentials and user information, and then uses JavaScript
     * to set local storage items such as access token, refresh token, store ID, user ID, and store owner ID. Finally, it
     * refreshes the page to apply the login session.
     *
     * @param credentials The credentials used to log in and retrieve user information.
     */
    public void loginDashboardByJs(APISellerLogin.Credentials credentials) {
        // Access the dashboard to set the cookie
        driver.get(PropertiesUtils.getDomain());

        // Initialize login information model
        APISellerLogin.LoginInformation loginInfo = new APISellerLogin().getSellerInformation(credentials);

        // Set local storage items using JavaScript
        webUtils.setLocalStorageValue("accessToken", loginInfo.getAccessToken());
        webUtils.setLocalStorageValue("refreshToken", loginInfo.getRefreshToken());
        webUtils.setLocalStorageValue("storeId", loginInfo.getStore().getId());
        webUtils.setLocalStorageValue("userId", loginInfo.getId());
        webUtils.setLocalStorageValue("storeOwnerId", loginInfo.getId());
        webUtils.setLocalStorageValue("storeFull", "storeFull");
        webUtils.setLocalStorageValue("langKey", loginInfo.getLangKey());

        logger.info("Set local storage successfully");

        // Refresh the page to apply the login session
        driver.navigate().refresh();
    }
}
