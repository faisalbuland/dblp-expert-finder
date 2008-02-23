
    create table Author (
        AUTHOR_NAME varchar(255) not null,
        primary key (AUTHOR_NAME)
    );

    create table Category (
        CAT_KEY varchar(255) not null,
        active smallint not null,
        id varchar(255),
        label varchar(255),
        parent_CAT_KEY varchar(255),
        primary key (CAT_KEY)
    );

    create table Cite (
        CITE_VALUE varchar(255) not null,
        label varchar(255),
        primary key (CITE_VALUE)
    );

    create table Journal (
        JOURNAL_NAME varchar(255) not null,
        primary key (JOURNAL_NAME)
    );

    create table Keyword (
        id bigint not null,
        keyword varchar(255),
        primary key (id)
    );

    create table PUBCONTENT_CATS (
        PublicationContent_id bigint not null,
        categories_CAT_KEY varchar(255) not null,
        primary key (PublicationContent_id, categories_CAT_KEY)
    );

    create table PUBCONTENT_KWS (
        PublicationContent_id bigint not null,
        keywords_id bigint not null,
        primary key (PublicationContent_id, keywords_id)
    );

    create table PUB_AUTHORS (
        PUB_ID varchar(255) not null,
        AUTHOR_ID varchar(255) not null,
        primary key (PUB_ID, AUTHOR_ID)
    );

    create table PUB_CITES (
        PUB_ID varchar(255) not null,
        CITE_ID varchar(255) not null,
        primary key (PUB_ID, CITE_ID)
    );

    create table Publication (
        DBLP_KEY varchar(255) not null,
        address varchar(255),
        booktitle varchar(255),
        CDROM_PATH varchar(255),
        chapter varchar(255),
        crossref varchar(255),
        editor varchar(255),
        ELECTRONIC_EDITION varchar(255),
        isbn varchar(255),
        MOD_DATE varchar(255),
        month varchar(255),
        note varchar(255),
        number varchar(255),
        pages varchar(255),
        series varchar(255),
        title varchar(4000),
        type integer,
        url varchar(255),
        volume varchar(255),
        PUB_YEAR varchar(255),
        school_SCHOOL_NAME varchar(255),
        publisher_PUBLISHER_NAME varchar(255),
        content_id bigint,
        journal_JOURNAL_NAME varchar(255),
        primary key (DBLP_KEY)
    );

    create table PublicationContent (
        id bigint not null,
        abstractText clob(1048576),
        primary key (id)
    );

    create table Publisher (
        PUBLISHER_NAME varchar(255) not null,
        href varchar(255),
        primary key (PUBLISHER_NAME)
    );

    create table School (
        SCHOOL_NAME varchar(255) not null,
        primary key (SCHOOL_NAME)
    );

    alter table Category 
        add constraint FK6DD211ED95D67CF 
        foreign key (parent_CAT_KEY) 
        references Category;

    alter table PUBCONTENT_CATS 
        add constraint FK4E6B8EE05AE61341 
        foreign key (categories_CAT_KEY) 
        references Category;

    alter table PUBCONTENT_CATS 
        add constraint FK4E6B8EE08E3B953F 
        foreign key (PublicationContent_id) 
        references PublicationContent;

    alter table PUBCONTENT_KWS 
        add constraint FK2BD20C8488A88614 
        foreign key (keywords_id) 
        references Keyword;

    alter table PUBCONTENT_KWS 
        add constraint FK2BD20C848E3B953F 
        foreign key (PublicationContent_id) 
        references PublicationContent;

    alter table PUB_AUTHORS 
        add constraint FK6E514CC6E7802CA4 
        foreign key (PUB_ID) 
        references Publication;

    alter table PUB_AUTHORS 
        add constraint FK6E514CC6B90CA1ED 
        foreign key (AUTHOR_ID) 
        references Author;

    alter table PUB_CITES 
        add constraint FK1C70D9FAE7802CA4 
        foreign key (PUB_ID) 
        references Publication;

    alter table PUB_CITES 
        add constraint FK1C70D9FA9460E72D 
        foreign key (CITE_ID) 
        references Cite;

    alter table Publication 
        add constraint FK23254A0C4FE907B2 
        foreign key (school_SCHOOL_NAME) 
        references School;

    alter table Publication 
        add constraint FK23254A0C79BEC1F 
        foreign key (journal_JOURNAL_NAME) 
        references Journal;

    alter table Publication 
        add constraint FK23254A0CABD3BA13 
        foreign key (content_id) 
        references PublicationContent;

    alter table Publication 
        add constraint FK23254A0C5270289A 
        foreign key (publisher_PUBLISHER_NAME) 
        references Publisher;

    create table hibernate_unique_key (
         next_hi integer 
    );

    insert into hibernate_unique_key values ( 0 );
