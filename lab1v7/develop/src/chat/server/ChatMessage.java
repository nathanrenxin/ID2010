package chat.server;

public class ChatMessage implements java.io.Serializable
{

  protected String text;
  protected String sender;

  public ChatMessage(String text, String sender) {
    this.text = text;
    this.sender = sender;
  }

  public String getText () {
    return text;
  }

  public String getSender () {
    return sender;
  }
}
