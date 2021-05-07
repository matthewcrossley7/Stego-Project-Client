package com.example.demo;


import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;



import java.io.*;
import java.util.Base64;
import java.util.Map;


@Controller
public class GETController {
    String address = "86.9.92.222";
    String msg;
    String filePath;
    String decodedImageMsg;
    Map userAttributes;
    imageMsg[]  imageArray;
    ChatClient serverAccess;

    //handles view profile header
    @GetMapping("/profile")
    public String displayProfile(Model model) {
        //add attributes that will be shown on html
        model.addAttribute("name", userAttributes.get("name"));
        model.addAttribute("email", userAttributes.get("email"));
        model.addAttribute("filePath", filePath);
        model.addAttribute("currPage", "profile");
        //returns required html page
        return "profile";
    }
    //handles when delete account button pressed
    @RequestMapping(value="/profile",params="delete",method=RequestMethod.POST)
    public RedirectView updatePath(Model model) throws JSONException, IOException {
        //create and send delete account JSON
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "deleteAccount");
        jsonObject.put("email", userAttributes.get("email"));
        serverAccess.sendJSON(jsonObject);
        JSONObject recieveJSON = serverAccess.recieveMsg();
        //redirect to logout page once account deleted
        return new RedirectView("/logout");
    }
    //handles when user wants to update their file path
    @RequestMapping(value="/profile",params="update",method=RequestMethod.POST)
    public String deleteAccount(Model model,@RequestParam String newFilePath) throws IOException, JSONException { //requests required parameters from form
        model.addAttribute("name", userAttributes.get("name"));
        model.addAttribute("email", userAttributes.get("email"));
        model.addAttribute("filePath", newFilePath);
        filePath=newFilePath;
        //create JSON object to send server
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "updatePath");
        jsonObject.put("email", userAttributes.get("email"));
        jsonObject.put("path", newFilePath);
        serverAccess.sendJSON(jsonObject);
        JSONObject recieveJSON = serverAccess.recieveMsg();
        model.addAttribute("currPage", "profile");
        //return required html page
        return "profile";
    }

    //handles when send heading is selected
    @GetMapping("/send")
    public String send(Model model,@RequestParam(name="email", required=false, defaultValue="null") String email,@RequestParam(defaultValue = "")  String status,@RequestParam(defaultValue = "")  String fileSent) throws JSONException, IOException {
        model.addAttribute("toEmail", email);
        model.addAttribute("image", "C:\\Users\\mcrossley\\IdeaProjects\\Stego Project Oauth\\demo\\src\\main\\java\\com\\example\\demo\\download.png");
        model.addAttribute("email", userAttributes.get("email"));
        model.addAttribute("name", userAttributes.get("name"));
        //adds attributed depending on GET value of 'status' of previous request
        if(status.equals("success")){
            model.addAttribute("status","success");
            model.addAttribute("sentFile",fileSent);
        }else if(status.equals("neither")){
            model.addAttribute("status","neither");
        }else if(status.equals("noImage")){
            model.addAttribute("status","noImage");
        }else if(status.equals("noEmail")){
            model.addAttribute("status","noEmail");
        }
        //create JSON object to request list of user's friends
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type","viewFriends");
        jsonObject.put("email",userAttributes.get("email"));
        serverAccess.sendJSON(jsonObject);
        JSONObject friendList = serverAccess.recieveMsg();
        //create array of list of friends
        String[] friendArray = new String[friendList.length()];
        for(int x=0;x<friendList.length();x++){
            //instantiate array values
            friendArray[x]= friendList.getString("email"+String.valueOf(x+1));
        }
        model.addAttribute("friendArray", friendArray);
        model.addAttribute("currPage", "send");
//return send html page
        return "send";
    }
    //called after user sends image to a friend
    @PostMapping("/send")
    public RedirectView sendPost(@RequestParam(defaultValue = "null")  String fileName,@RequestParam String email,@RequestParam String friend, Model model) throws IOException, JSONException {
        System.out.println("---"+friend+"----"+fileName);
        //handles if no friend or image selected
        if(friend.equals("unselected") && fileName.equals("null")){
            return new RedirectView("http://localhost:8081/send?status=neither");   //redirect to URL
        }
        //handles if no image selected
        if(fileName.equals("null")){
            model.addAttribute("email", email);
            System.out.println("---"+email);
            return new RedirectView("http://localhost:8081/send?status=noImage");
        }
        //handles if no friend selected
        if(friend.equals("unselected")){
            model.addAttribute("email", email);
            System.out.println("YOU DIDNT CHOOSE AN EMAIL");
            return new RedirectView("http://localhost:8081/send?status=noEmail");
        }

        //create full file path of image location
        String completePath = filePath+ "\\"+ fileName;

     //convert image to base64 string. Code taken from https://grokonez.com/java/java-advanced/java-8-encode-decode-an-image-base64
        File file = new File(completePath);
        FileInputStream imageInFile = new FileInputStream(file);
        byte imageData[] = new byte[(int) file.length()];
        imageInFile.read(imageData);
        String imageDataString = Base64.getEncoder().encodeToString(imageData);

        imageInFile.close();
        //create JSON object to send to server
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type","sendImage");
        jsonObject.put("fromEmail",userAttributes.get("email"));
        jsonObject.put("toEmail",friend);
        jsonObject.put("image",imageDataString);

        serverAccess.sendJSON(jsonObject);
        JSONObject recieveJSON = serverAccess.recieveMsg();

        //redirect to URL
        return new RedirectView("/send?status=success&fileSent="+fileName);
    }
    //handles when user selects a friend from dropdown box
    @RequestMapping(value="/viewMsgs",params="selectUser",method=RequestMethod.POST)
    public RedirectView selectUser(@RequestParam String selectUser) throws IOException {

        if(selectUser.charAt(selectUser.length()-1)==','){
            selectUser = selectUser.substring(0,selectUser.length()-1);
        }
        //redirect to URL and POST email of friend selected
        return new RedirectView("/viewMsgs?email="+selectUser);
    }
    //handles when user selects View Messages
    @GetMapping("/viewMsgs")
    public String viewMsgs(@RequestParam(defaultValue = "")  String email,Model model,@RequestParam(defaultValue = "")  String downloadStatus,@RequestParam(defaultValue = "")  String deleteStatus,@RequestParam(defaultValue = "")  String msg) throws JSONException, IOException {
        //adds information to html page displayed depending on GET value
        if(downloadStatus.equals("success")){
            model.addAttribute("status", "downloaded");
        }
        if(deleteStatus.equals("success")){
            model.addAttribute("status", "deleted");
        }
        if(!msg.equals("")){
            model.addAttribute("msg", decodedImageMsg);
        }
        model.addAttribute("email", userAttributes.get("email"));
        model.addAttribute("name", userAttributes.get("name"));
        JSONObject jsonObject = new JSONObject();

        if(!email.equals("")){
            //create JSON object to request messages with a specific user
            jsonObject.put("type","viewMsgs");
            jsonObject.put("email",userAttributes.get("email"));
            jsonObject.put("fromEmail",email);
            serverAccess.sendJSON(jsonObject);

            //create arrays to hold information about the messages
            JSONObject recieveJSON = serverAccess.recieveMsg();
            String[] imageFrom = new String[recieveJSON.length()];
            String[] imageTo = new String[recieveJSON.length()];
            String[] imageTime = new String[recieveJSON.length()];
            String[] imageString = new String[recieveJSON.length()];
            imageArray = new imageMsg[recieveJSON.length()];
            for(int x=0;x<recieveJSON.length();x++){
                JSONObject imageJson = new JSONObject(recieveJSON.getString("image"+String.valueOf(x+1)));
                //depending on who message is from different arrays are added to
                if(imageJson.getString("fromEmail")==userAttributes.get("email")){
                    imageFrom[x]=imageJson.getString("fromEmail");
                    imageTo[x]= (String) userAttributes.get("email");
                }else{
                    imageTo[x]=imageJson.getString("fromEmail");
                    imageFrom[x]= (String) userAttributes.get("email");
                }
                //add value to array holding time message is sent from JSON object
                  imageTime[x]=imageJson.getString("time");
                imageArray[x] = new imageMsg(imageJson.getString("fromEmail"),imageJson.getString("time"),imageJson.getString("image"),imageJson.getString("toEmail"),imageJson.getInt("imageID"));
                imageString[x]= imageJson.getString("fromEmail")+"  at time " + imageJson.getString("time");
            }
            //add information that will be displayed on the html page
            model.addAttribute("imageTo", imageTo);
            model.addAttribute("imageFrom", imageFrom);
            model.addAttribute("imageTime", imageTime);
            model.addAttribute("imageArray", imageArray);
            model.addAttribute("fromEmail", email);
            model.addAttribute("name", userAttributes.get("name"));
            model.addAttribute("email", userAttributes.get("email"));
        }
        //create JSON object to request list of user's friends
        jsonObject = new JSONObject();
        jsonObject.put("type","viewFriends");
        jsonObject.put("email",userAttributes.get("email"));
        serverAccess.sendJSON(jsonObject);
        JSONObject friendList = serverAccess.recieveMsg();
        String[] friendArray = new String[friendList.length()];
        model.addAttribute("friendArray", friendArray);
        for(int x=0;x<friendList.length();x++){
            friendArray[x]= friendList.getString("email"+String.valueOf(x+1));  //initiate list of friends array
        }
        model.addAttribute("currPage", "viewMsgs");

        //return viewMsgs html page
        return "viewMsgs";
    }
    //handles when user wants to download an image
    @RequestMapping(value="/viewMsgs",params="imageString",method=RequestMethod.POST)
    public RedirectView image1(@RequestParam  String imageString,@RequestParam String fileName,@RequestParam(defaultValue = "") String email) throws IOException {

        //decodes specified image and saves image to specified location
        String base64Image = imageArray[Integer.parseInt(imageString)].image;
        byte[] imageByteArray = Base64.getDecoder().decode(base64Image);
        FileOutputStream imageOutFile = new FileOutputStream(filePath+"\\"+fileName+".png");
        imageOutFile.write(imageByteArray);
        imageOutFile.close();

        //redirects to required URL
        if(email.equals("")){
            return new RedirectView("/viewMsgs?downloadStatus=success");
        }else{
            return new RedirectView("/viewMsgs?email="+email+"&downloadStatus=success");
        }

    }
    //handles when user wants to delete an image
    @RequestMapping(value="/viewMsgs",params="delete",method=RequestMethod.POST)
    public RedirectView delete(@RequestParam  String delete,@RequestParam(defaultValue = "") String email) throws JSONException, IOException {
        //Create JSON object to delete specified image
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type","deleteMsg");
        //add the id of message to delete
        jsonObject.put("imageID",imageArray[Integer.parseInt(delete)].imageID);
        serverAccess.sendJSON(jsonObject);
        JSONObject result = serverAccess.recieveMsg();
        //redirects to URL
        if(email.equals("")){
            return new RedirectView("/viewMsgs?deleteStatus=success");
        }else{
            return new RedirectView("/viewMsgs?email="+email+"&deleteStatus=success");
        }
    }
    //handles when user wants to extract a message from the image
    @RequestMapping(value="/viewMsgs",params="decode",method=RequestMethod.POST)
    public RedirectView decode(@RequestParam  String decode,@RequestParam  String key,@RequestParam(defaultValue = "") String email) throws IOException {

        //create an image variable of message
        image theImage = new image();
        theImage.setString(imageArray[Integer.parseInt(decode)].image);
        //find centre of image
        imageCentre center = theImage.isCentre();

        if(center==null){
            //if image isn't valid redirect
            return new RedirectView("/viewMsgs?email="+email+"&msg=No message found");

        }else{
            int length = theImage.getMsgLength(center);
            //extract message from image
            String msg = theImage.extractData(center);

            if(!key.equals("")) {
                //decrypt message if key provided
                msg = decryptMsg(msg,key);
            }
            decodedImageMsg = msg;
            //redirect to URL
            return new RedirectView("/viewMsgs?email="+email+"&msg=true");
        }

    }


   //handles when user selects View Friends page
    @GetMapping("/viewFriends")
    public String viewFriends(Model model,@RequestParam(defaultValue = "")  String removeFriend) throws IOException, JSONException {

        JSONObject jsonObject = new JSONObject();

        //if user wants to delete friend create JSON object
        if(!removeFriend.equals("")){
            jsonObject.put("type","deleteFriend");
            jsonObject.put("email1",userAttributes.get("email"));
            jsonObject.put("email2",removeFriend);
            serverAccess.sendJSON(jsonObject);
            JSONObject isSuccess = serverAccess.recieveMsg();
        }
        //create JSON object to view list of friends
        jsonObject = new JSONObject();
        jsonObject.put("type","viewFriends");
        jsonObject.put("email",userAttributes.get("email"));
        serverAccess.sendJSON(jsonObject);
        JSONObject friendList = serverAccess.recieveMsg();
        //create and instantiate array of friends
        String[] friendArray = new String[friendList.length()];
        for(int x=0;x<friendList.length();x++){
            friendArray[x]= friendList.getString("email"+String.valueOf(x+1));
        }
        //add information to html page
        model.addAttribute("friendArray", friendArray);
        model.addAttribute("name", userAttributes.get("name"));
        model.addAttribute("email", userAttributes.get("email"));
        model.addAttribute("currPage", "viewFriends");
        //return html page
        return "viewFriends";
    }
    //called if user wants to add a new friend
    @PostMapping("/viewFriends")
    public RedirectView viewFriendsPost(Model model,@RequestParam  String friendEmail) throws JSONException, IOException {

        if(!friendEmail.equals(userAttributes.get("email"))){
            //create JSON object to delete user as friend
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", "addFriend");
            jsonObject.put("userEmail", userAttributes.get("email"));
            jsonObject.put("friendEmail", friendEmail);
            serverAccess.sendJSON(jsonObject);
            JSONObject recieveJSON = serverAccess.recieveMsg();
        }else{
            //if user requests to add himself
            model.addAttribute("name", "You cant add yourself");
        }
        return new RedirectView("/viewFriends");
    }
    //called when user is signing in for the first time
    @GetMapping("/register")
    public String register(Model model) {

        model.addAttribute("name", userAttributes.get("name"));
        model.addAttribute("email", userAttributes.get("email"));
        //return html page
        return "register";
    }
    //called when user enters file path to register in system
    @PostMapping ("/register")
    public RedirectView registerInDB(Model model,@RequestParam  String filePath) throws JSONException, IOException {
        //create JSON object to enter user's file path in database
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type","register");
        jsonObject.put("table","user");
        jsonObject.put("email",userAttributes.get("email"));
        jsonObject.put("path",filePath);
        serverAccess.sendJSON(jsonObject);
        JSONObject returnJSON = serverAccess.recieveMsg();
        //updates file path variable
        this.filePath=filePath;
        //redirect to homepage after registering
        return new RedirectView("/homepage");
    }
    //called when user visits encode page
    @GetMapping("/encode")
    public String encode(@RequestParam(name="user", required=false, defaultValue="defaultUser") String name,Model model) throws IOException, JSONException {
        //Create JSON object to view list of friends
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type","viewFriends");
        jsonObject.put("email",userAttributes.get("email"));
        serverAccess.sendJSON(jsonObject);
        JSONObject friendList = serverAccess.recieveMsg();
        //create array to store list of friends
        String[] friendArray = new String[friendList.length()];
        for(int x=0;x<friendList.length();x++){
            friendArray[x]= friendList.getString("email"+String.valueOf(x+1));
        }

        model.addAttribute("friendArray", friendArray);
        model.addAttribute("name", userAttributes.get("name"));
        model.addAttribute("email", userAttributes.get("email"));
        model.addAttribute("msg", msg);
        model.addAttribute("currPage", "encode");
        //return encode html page
        return "encode";
    }
   //called when user selects create stego-image
    @RequestMapping(value = "/encode", method = RequestMethod.POST, params = "encode")
    public String encodeImagePost(Model model,@RequestParam String fileName,@RequestParam String msg,@RequestParam String key,@RequestParam String newFileName,@RequestParam(name="bitDepth", required=false, defaultValue="1") int bitDepth,@RequestParam(name="separation", required=false, defaultValue="0") int separation,@RequestParam String fillImage) throws JSONException, IOException {
         String encryptedMsg;
        //encrypt message if key provided
        if(!key.equals("")){
            encryptedMsg=encryptMsg(msg,key);
        }else{
            encryptedMsg = msg;
        }
        int length = encryptedMsg.length();

        //read in image selected
        image theImage = new image(filePath+"\\"+fileName);
        theImage.readImage();
        theImage.readPixels();

        if(fillImage.equals("Y")){
            //calculated maximum separation if option selected
            separation = theImage.getMaxSeparation(length,bitDepth);
            theImage.hideMetaData(length,bitDepth,separation);
        }
        //hide the length of message, number of LSBs used and separation between pixels in cover image
        theImage.hideMetaData(length,bitDepth,separation);
        //hide the contents of the message, also obtains number of characters not successfully encoded
        int numOutOfRange = theImage.hideMessage(encryptedMsg);

        //handle if multiple sequences of bits representing centre present
        if(theImage.isDoubleCentre()){
            model.addAttribute("status","failure");
        }else if(numOutOfRange>0){
            model.addAttribute("status","partial");
            model.addAttribute("msg",msg);
            model.addAttribute("failed",(int)Math.ceil((double) numOutOfRange/3));
            //save created stego-image
            theImage.saveNewImage(filePath+"\\"+newFileName+".png");
        }else{
            model.addAttribute("status","success");
            theImage.saveNewImage(filePath+"\\"+newFileName+".png");

            msg = "'"+msg+"'";
            model.addAttribute("msg",msg);
            newFileName=newFileName+".png";
            model.addAttribute("fileName",newFileName);
            model.addAttribute("separation",separation);
            model.addAttribute("bitDepth",bitDepth);
        }
        model.addAttribute("name", userAttributes.get("name"));
        model.addAttribute("email", userAttributes.get("email"));
        model.addAttribute("currPage", "encode");
        //return encode html page
        return "encode";
    }
    //called if user requests to encode secret message and send in one step
    @RequestMapping(value = "/encode", method = RequestMethod.POST, params = "encodeSend")
    public String encodeImagePostSend(Model model,@RequestParam String friend, @RequestParam String fileName,@RequestParam String msg,@RequestParam String key,@RequestParam(name="bitDepth", required=false, defaultValue="1") int bitDepth,@RequestParam(name="separation", required=false, defaultValue="0") int separation,@RequestParam String fillImage) throws JSONException, IOException {
        //handles if no friend selected
        if(friend.equals("unselected")){
            model.addAttribute("status","noFriendSelected");
            return "encode";
        }
        //encrypt message if needed
        String encryptedMsg;
        if(!key.equals("")){
            encryptedMsg=encryptMsg(msg,key);
        }else{
            encryptedMsg = msg;
        }

        int length = encryptedMsg.length();
        image theImage = new image(filePath+"\\"+fileName);
        theImage.readImage();
        theImage.readPixels();
        if(fillImage.equals("Y")){
            //find max separation if required
            separation = theImage.getMaxSeparation(length,bitDepth);
        }
        //hide message meta data
        theImage.hideMetaData(length,bitDepth,separation);
        int numOutOfRange = theImage.hideMessage(encryptedMsg);
        if(theImage.isDoubleCentre()){
            model.addAttribute("status","failure");
        }else if(numOutOfRange>0){
            model.addAttribute("status","partial");
            model.addAttribute("msg",msg);
            model.addAttribute("failed",(int)Math.ceil((double) numOutOfRange/3));
        }else{
            model.addAttribute("status","success");

            msg = "'"+msg+"'";
            model.addAttribute("msg",msg);
            model.addAttribute("separation",separation);
            model.addAttribute("bitDepth",bitDepth);
        }
        //temporarily save created image so it can be converted to base 64 string and sent to server
        theImage.saveNewImage(filePath+"\\tempImagetoSend.png");
        File file = new File(filePath+"\\tempImagetoSend.png");
        FileInputStream imageInFile = new FileInputStream(file);
        byte imageData[] = new byte[(int) file.length()];
        imageInFile.read(imageData);
        String imageDataString = Base64.getEncoder().encodeToString(imageData);
        imageInFile.close();
        //create JSON object to send to server
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type","sendImage");
        jsonObject.put("fromEmail",userAttributes.get("email"));
        jsonObject.put("toEmail",friend);
        jsonObject.put("image",imageDataString);
        serverAccess.sendJSON(jsonObject);
        JSONObject recieveJSON = serverAccess.recieveMsg();
        //delete temporary image file
        file.delete();
        model.addAttribute("name", userAttributes.get("name"));
        model.addAttribute("email", userAttributes.get("email"));
        model.addAttribute("currPage", "encode");
        //return encode html page
        return "encode";
    }
    //called when user navigates to decode page
    @GetMapping("/decode")
    public String decode(@RequestParam(name="user", required=false, defaultValue="defaultUser") String name, Model model) throws IOException {
        model.addAttribute("name", userAttributes.get("name"));
        model.addAttribute("email", userAttributes.get("email"));

        model.addAttribute("currPage", "decode");
        //returns decode html page
        return "decode";
    }
    //called when user is extracting a message from an image
    @PostMapping("/decode")
    public String decodeImage(Model model,@RequestParam String fileName,@RequestParam String key) throws JSONException, IOException {
        //read in specified image
        image theImage = new image(filePath+"\\"+fileName);
        theImage.readImage();
       //located the centre of the image
        imageCentre center = theImage.isCentre();
        if(center==null){
            model.addAttribute("msg","error couldn't find any message");
        }else{
            int length = theImage.getMsgLength(center);
            String msg = theImage.extractData(center);
            //decrypt message if key provided
            if(!key.equals("")) {
                msg = decryptMsg(msg,key);
            }
            model.addAttribute("msg",msg);
        }
        model.addAttribute("name", userAttributes.get("name"));
        model.addAttribute("email", userAttributes.get("email"));
        model.addAttribute("fileName",fileName);
        model.addAttribute("currPage", "decode");
        //return html page
        return "decode";
    }
   //handles when user is viewing the homepage
    @GetMapping("/homepage")
    public String test(@RequestParam(name="user", required=false, defaultValue="defaultUser") String name, Model model) throws IOException, JSONException {
        model.addAttribute("name", userAttributes.get("name"));
        model.addAttribute("email", userAttributes.get("email"));

        model.addAttribute("currPage", "home");
        //return html page
        return "homepage";
    }

    //redirects to homepage
    @GetMapping("/")
    public RedirectView homepage() {

        return new RedirectView("/homepage");
    }
    //handles failed sign in attempts
    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;
    @GetMapping("/loginFailure")
    public String failedLogin(){
        System.out.println("you failed to login ");
        return "loginFailure";
    }
    //handles successful sign in
    @GetMapping("/loginSuccess")
    public RedirectView getLoginInfo(Model model, OAuth2AuthenticationToken authentication) throws IOException, JSONException {

        OAuth2AuthorizedClient client = authorizedClientService
                .loadAuthorizedClient(
                        authentication.getAuthorizedClientRegistrationId(),
                        authentication.getName());
        String userInfoEndpointUri = client.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUri();

        if (!StringUtils.isEmpty(userInfoEndpointUri)) {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + client.getAccessToken()
                    .getTokenValue());
            HttpEntity entity = new HttpEntity("", headers);
            ResponseEntity<Map> response = restTemplate
                    .exchange(userInfoEndpointUri, HttpMethod.GET, entity, Map.class);
            userAttributes = response.getBody();
            model.addAttribute("name", userAttributes.get("name"));
        //Besides the name, the userAttributes Map also contains properties such as email, family_name, picture, locale.
        }

        serverAccess = new ChatClient();
        int port =1777 ;


        //connect to server
        serverAccess.SetPortAddress(address, port);
        //create login JSON request
        JSONObject json = new JSONObject();
        json.put("type","login");
        json.put("email",userAttributes.get("email"));
        serverAccess.sendJSON(json);
        JSONObject reply = serverAccess.recieveMsg();

        if(reply.getBoolean("isRegistered")==false){
            //redirects to register page if new user
            return new RedirectView("/register");
        }else{
            //redirects to homepage if existing user
            filePath= reply.getString("filePath");

            return new RedirectView("/homepage");

        }
    }
    //function encrypts a given message with a given key
    private String encryptMsg(String msg, String key){
        String encryptedMsg;
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < msg.length(); i++) {
            sb.append((char)(msg.charAt(i) ^ key.charAt(i % key.length())));
            //XORs each character of message with a character from key
        }
        encryptedMsg = sb.toString();
        sb = new StringBuilder();
        int blocks = (int)Math.floor((double)msg.length()/(double) key.length());
        for(int x = 0;x<blocks;x++){
            for(int y = 0;y<key.length();y++){
                sb.append(encryptedMsg.charAt((x+1)*key.length()-y-1));
                //Rearrange order of characters in message
            }
        }
        for(int x= blocks*key.length();x<encryptedMsg.length();x++){
            sb.append(encryptedMsg.charAt(x));
            //Handles outlying characters in case message length is not divisible by key length
        }
        encryptedMsg=sb.toString();
        //returns encrypted message
        return encryptedMsg;
    }
    //decrypt provided message with provided key
    private String decryptMsg(String msg,String key){
        StringBuilder sb = new StringBuilder();
        String decryptedMsg;
        int blocks = (int)Math.floor((double)msg.length()/(double) key.length());
        for(int x = 0;x<blocks;x++){
            for(int y = 0;y<key.length();y++){
                sb.append(msg.charAt((x+1)*key.length()-y-1));
                //Rearrange order of secret message
            }
        }
        for(int x= blocks*key.length();x<msg.length();x++){
            sb.append(msg.charAt(x));
            //Handles outlying characters in case message length is not divisible by key length
        }
        decryptedMsg = sb.toString();
        sb = new StringBuilder();
        for (int i = 0; i < msg.length(); i++) {
            sb.append((char) (decryptedMsg.charAt(i) ^ key.charAt(i % key.length())));
            //XORs each character of encrypted message with character from key
        }
        decryptedMsg = sb.toString();
        //return decrypted message
        return decryptedMsg;
    }
}
