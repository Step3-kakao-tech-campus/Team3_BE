from flask import Flask, request, Response
import smtplib
from email.mime.text import MIMEText

app = Flask(__name__)

# HTTP POST 요청을 처리하는 엔드포인트
@app.route('/email', methods=['POST'])
def sendEmailEndpoint():

    try:
        jsonRequest = request.get_json()

        subject = str(jsonRequest.get('subject')[0])
        text = str(jsonRequest.get('text')[0])
        email = str(jsonRequest.get('email')[0])
        username = str(jsonRequest.get('username')[0])
        password = str(jsonRequest.get('password')[0])

        smtp = smtplib.SMTP('smtp.gmail.com', 587)
        smtp.ehlo()
        smtp.starttls()
        smtp.login(username, password)

        msg = MIMEText(text, "html")
        msg['Subject'] = subject

        smtp.sendmail(username, email, msg.as_string())
        smtp.quit()

        response = Response("Email sent successfully", status=200)

        return response

    except Exception as e:
        error_message = str(e)
        response = Response("Failed to send email: " + error_message, status=500)

        return response

if __name__ == '__main__':
    app.run('0.0.0.0', port=5000, debug=True)