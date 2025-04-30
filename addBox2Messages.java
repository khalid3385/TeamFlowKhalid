package volter.com.example.appscrumteam;

public abstract class addBox2Messages {
    private final String message;

    public addBox2Messages(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public abstract boolean hasCheckbox();
}

class UserMessage extends addBox2Messages {
    public UserMessage(String message) {
        super(message);
    }

    @Override
    public boolean hasCheckbox() {
        return true;
    }
}

class SystemMessage extends addBox2Messages {
    public SystemMessage(String message) {
        super(message);
    }

    @Override
    public boolean hasCheckbox() {
        return false;
    }
}
