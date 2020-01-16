<?php
// Import PHPMailer classes into the global namespace
// These must be at the top of your script, not inside a function
use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\SMTP;
use PHPMailer\PHPMailer\Exception;

require_once('DBOperations.php');
require('vendor/autoload.php');


class Functions{
    private $db;
    private $mail;

    public function __construct(){
        $this->db = new DBOperations();
        $this->mail = new PHPMailer(true);
    }

    public function registerUser($name, $email, $password) {
        $db = $this -> db;
        if (!empty($name) && !empty($email) && !empty($password)) {
           if ($db -> checkUserExist($email)) {
              $response["result"] = "failure";
              $response["message"] = "User Already Registered !";
              return json_encode($response);
           } else {
              $result = $db -> insertData($name, $email, $password);
              if ($result) {
                   $response["result"] = "success";
                 $response["message"] = "User Registered Successfully !";
                 return json_encode($response);
              } else {
                 $response["result"] = "failure";
                 $response["message"] = "Registration Failure";
                 return json_encode($response);
              }
           }
        } else {
           return $this -> getMsgParamNotEmpty();
        }
     }

     public function loginUser($email, $password) {
        $db = $this -> db;
        if (!empty($email) && !empty($password)) {
          if ($db -> checkUserExist($email)) {
             $result =  $db -> checkLogin($email, $password);
             if(!$result) {
              $response["result"] = "failure";
              $response["message"] = "Invaild Login Credentials";
              return json_encode($response);
             } else {
              $response["result"] = "success";
              $response["message"] = "Login Sucessful";
              $response["user"] = $result;
              return json_encode($response);
             }
          } else {
            $response["result"] = "failure";
            $response["message"] = "Invaild Login Credentials";
            return json_encode($response);
          }
        } else {
            return $this -> getMsgParamNotEmpty();
          }
      }

      public function changePassword($email, $old_password, $new_password) {
        $db = $this -> db;
        if (!empty($email) && !empty($old_password) && !empty($new_password)) {
          if(!$db -> checkLogin($email, $old_password)){
            $response["result"] = "failure";
            $response["message"] = 'Invalid Old Password';
            return json_encode($response);
          } else {
          $result = $db -> changePassword($email, $new_password);
            if($result) {
              $response["result"] = "success";
              $response["message"] = "Password Changed Successfully";
              return json_encode($response);
            } else {
              $response["result"] = "failure";
              $response["message"] = 'Error Updating Password';
              return json_encode($response);
            }
          }
        } else {
            return $this -> getMsgParamNotEmpty();
        }
      }
      
      public function isEmailValid($email){
        return filter_var($email, FILTER_VALIDATE_EMAIL);
      }
      
      public function getMsgParamNotEmpty(){
        $response["result"] = "failure";
        $response["message"] = "Parameters should not be empty !";
        return json_encode($response);
      }
      
      public function getMsgInvalidParam(){
        $response["result"] = "failure";
        $response["message"] = "Invalid Parameters";
        return json_encode($response);
      
      }
      
      public function getMsgInvalidEmail(){
        $response["result"] = "failure";
        $response["message"] = "Invalid Email";
        return json_encode($response);
      
      }

      public function resetPasswordRequest($email){

        $db = $this -> db;

        if ($db -> checkUserExist($email)) {

            $result =  $db -> passwordResetRequest($email);

            if(!$result){

                $response["result"] = "failure";
                $response["message"] = "Reset Password Failure";
                return json_encode($response);

            } else {

                $mail_result = $this -> sendEmail($result["email"],$result["temp_password"]);

                if($mail_result){

                    $response["result"] = "success";
                    $response["message"] = "Check your mail for reset password code.";
                    return json_encode($response);

                } else {

                    $response["result"] = "failure";
                    $response["message"] = "Reset Password Failure";
                    return json_encode($response);
                }
            }
        } else {

            $response["result"] = "failure";
            $response["message"] = "Email does not exist";
            return json_encode($response);

        }
    }

    public function resetPassword($email,$code,$password){

        $db = $this -> db;

        if ($db -> checkUserExist($email)) {

            $result =  $db -> resetPassword($email,$code,$password);

            if(!$result){

                $response["result"] = "failure";
                $response["message"] = "Reset Password Failure";
                return json_encode($response);

            } else {

                $response["result"] = "success";
                $response["message"] = "Password Changed Successfully";
                return json_encode($response);

            }
        } else {

            $response["result"] = "failure";
            $response["message"] = "Email does not exist";
            return json_encode($response);

        }
    }

    public function sendEmail($email,$temp_password){

        $mail = $this -> mail;
        $mail->isSMTP();// Set mailer to use SMTP
        $mail->CharSet = "utf-8";// set charset to utf8
        $mail->SMTPAuth = true;// Enable SMTP authentication
        $mail->SMTPSecure = 'tls';// Enable TLS encryption, `ssl` also accepted

        $mail->Host = 'smtp.gmail.com';// Specify main and backup SMTP servers
        $mail->Port = 587;// TCP port to connect to
        $mail->SMTPOptions = array(
            'ssl' => array(
                'verify_peer' => false,
                'verify_peer_name' => false,
                'allow_self_signed' => true
            )
        );
        $mail->isHTML(true);// Set email format to HTML
        $mail->Username = 'email@example.com';
        $mail->Password = 'yffcrjkuihzdygwa';

        $mail->From = 'email@example.com';
        $mail->FromName = 'Nama Aplikasi';
        $mail->addAddress($email, 'Nama Aplikasi');

        $mail->addReplyTo('hunterdumay@gmail.com', 'Nama Aplikasi');

        $mail->WordWrap = 50;
        $mail->isHTML(true);

        $mail->Subject = 'Password Reset Request';
        $mail->Body    = 'Hi,<br><br> Your password reset code is <b>'.$temp_password.'</b> . This code expires in 120 seconds. Enter this code within 120 seconds to reset your password.<br><br>Thanks,<br>Nama Aplikasi.';

        if(!$mail->send()) {

            return $mail->ErrorInfo;

        } else {

            return true;

        }
    }

    public function sendPHPMail($email,$temp_password){

        $subject = 'Password Reset Request';
        $message = 'Hi,nn Your password reset code is '.$temp_password.' . This code expires in 120 seconds. Enter this code within 120 seconds to reset your password.nnThanks,nLearn2Crack.';
        $from = "email@example.com";
        $headers = "From:" . $from;

        return mail($email,$subject,$message,$headers);

    }
      
}