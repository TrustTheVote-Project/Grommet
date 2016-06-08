package com.rockthevote.grommet.data.api.model;


public final class Voter {

    public final boolean is_us_citizen;
    public final boolean is_of_age;

    public final Title title;
    public final String first_name;
    public final String last_name;
    public final Suffix suffix;

    public final     String address;
    public final String zip_code;
    public final String birthday;

    public final String email;
    public final String phone_number;
    public final PhoneType phone_type;

    public final String school;
    public final String issue;

    public final boolean can_receive_email;
    public final boolean can_receive_text;


    private Voter(Builder builder) {

        this.is_us_citizen = builder.is_us_citizen;
        this.is_of_age = builder.is_of_age;
        this.title = builder.title;
        this.first_name = builder.first_name;
        this.last_name = builder.last_name;
        this.suffix = builder.suffix;
        this.address = builder.address;
        this.zip_code = builder.zip_code;
        this.birthday = builder.birthday;
        this.email = builder.email;
        this.phone_number = builder.phone_number;
        this.phone_type = builder.phone_type;
        this.school = builder.school;
        this.issue = builder.issue;
        this.can_receive_email = builder.can_receive_email;
        this.can_receive_text = builder.can_receive_text;
    }

    public static final class Builder {
        private boolean is_us_citizen;
        private boolean is_of_age;

        private Title title = Title.EMPTY;
        private String first_name;
        private String last_name;
        private Suffix suffix = Suffix.EMPTY;

        private String address;
        private String zip_code;
        private String birthday;

        private String email;
        private String phone_number;
        private PhoneType phone_type = PhoneType.MOBILE;

        private String school;
        private String issue;

        private boolean can_receive_email;
        private boolean can_receive_text;

        public Builder isUsCitizen(Boolean isUSCitizen) {
            this.is_us_citizen = isUSCitizen;
            return this;
        }

        public Builder isOfAge(Boolean value) {
            this.is_of_age = value;
            return this;
        }

        public Builder title(Title value) {
            this.title = value;
            return this;
        }

        public Builder firstName(String value) {
            this.first_name = value;
            return this;
        }

        public Builder lastName(String value) {
            this.last_name = value;
            return this;
        }

        public Builder suffix(Suffix value) {
            this.suffix = value;
            return this;
        }

        public Builder address(String value) {
            this.address = value;
            return this;
        }

        public Builder zipCode(String value) {
            this.zip_code = value;
            return this;
        }

        public Builder birthday(String value) {
            this.birthday = value;
            return this;
        }

        public Builder email(String value) {
            this.email = value;
            return this;
        }

        public Builder phoneNumber(String value) {
            this.phone_number = value;
            return this;
        }

        public Builder phoneType(PhoneType value) {
            this.phone_type = value;
            return this;
        }

        public Builder school(String value) {
            this.school = value;
            return this;
        }

        public Builder issue(String value) {
            this.issue = value;
            return this;
        }

        public Builder canReceiveEmail(Boolean value) {
            this.can_receive_email = value;
            return this;
        }

        public Builder canRecieveTexts(Boolean value) {
            this.can_receive_text = value;
            return this;
        }

        public Voter build() {
            return new Voter(this);
        }

    }

}
