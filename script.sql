create table news
(
    id          bigint auto_increment
        primary key,
    title       varchar(200)                           not null comment '新闻标题',
    content     text                                   null comment '新闻内容',
    source      varchar(100) default ''                null comment '来源',
    image_url   varchar(512) default ''                null comment '封面图',
    create_time timestamp    default CURRENT_TIMESTAMP null
)
    comment '校园新闻/公告表';

create table post
(
    id           bigint auto_increment
        primary key,
    user_id      bigint                             null comment '用户ID',
    post_title   varchar(128)                       null comment '帖子标题',
    post_content text                               null,
    is_deleted   int      default 0                 null comment '逻辑删除',
    creat_time   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
);

create table post_data
(
    id          bigint auto_increment
        primary key,
    post_id     bigint                             null comment '帖子ID',
    status      char(2)                            null comment '01 点赞 02 浏览量 03 转发',
    number      bigint   default 0                 null comment '数量',
    is_deleted  int      default 0                 null comment '逻辑删除',
    creat_time  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
);

create table resource
(
    id               bigint auto_increment comment '资源ID'
        primary key,
    name             varchar(200)                           not null comment '资源名称',
    description      text                                   null comment '资源描述',
    resource_type    varchar(20)  default 'PHYSICAL'        not null comment '类型：DIGITAL/PHYSICAL',
    category         varchar(50)  default '其他'            null comment '分类',
    points_cost      int          default 10                not null comment '借用所需积分',
    status           varchar(20)  default 'AVAILABLE'       not null comment '状态：AVAILABLE/LENT/OFFLINE',
    owner_id         bigint                                 not null comment '分享者用户ID',
    owner_address    varchar(256) default ''                null comment '分享者区块链地址',
    image_url        varchar(512) default ''                null comment '资源图片URL',
    borrower_id      bigint                                 null comment '当前借用者ID',
    borrow_count     int          default 0                 null comment '累计借用次数',
    create_time      timestamp    default CURRENT_TIMESTAMP null comment '发布时间',
    update_time      timestamp    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    borrower_address varchar(256)                           null comment '借用者公钥地址',
    borrow_end_time  datetime                               null comment '应归还截止时间'
)
    comment '校园共享资源表';

create index idx_borrower
    on resource (borrower_id);

create index idx_category
    on resource (category);

create index idx_owner
    on resource (owner_id);

create index idx_status
    on resource (status);

create table sign_in
(
    id             bigint auto_increment
        primary key,
    user_id        bigint                             not null comment '用户ID',
    sign_date      date                               not null comment '签到日期',
    points_awarded int      default 5                 null comment '获得积分',
    create_time    datetime default CURRENT_TIMESTAMP null,
    constraint uk_user_date
        unique (user_id, sign_date)
);

create table transaction_record
(
    id                bigint auto_increment comment '记录ID'
        primary key,
    sender_address    varchar(256)                           not null comment '借用者公钥地址',
    recipient_address varchar(256)                           not null comment '分享者公钥地址',
    amount            int          default 0                 not null comment '积分数量',
    timestamp         varchar(50)                            not null comment '交易时间戳',
    resource_id       bigint                                 null comment '关联的资源ID',
    resource_name     varchar(200) default ''                null comment '资源名称',
    resource_type     varchar(20)  default ''                null comment '资源类型',
    record_type       varchar(30)                            not null comment '记录类型',
    tx_hash           varchar(256) default ''                null comment '交易哈希',
    block_number      bigint                                 null comment '所在区块号',
    create_time       timestamp    default CURRENT_TIMESTAMP null comment '入库时间'
)
    comment '资源共享交易记录表';

create index idx_block
    on transaction_record (block_number);

create index idx_recipient
    on transaction_record (recipient_address);

create index idx_record_type
    on transaction_record (record_type);

create index idx_resource
    on transaction_record (resource_id);

create index idx_sender
    on transaction_record (sender_address);

create table user
(
    id             bigint auto_increment
        primary key,
    phone          char(11)                               null,
    password       varchar(255)                           null,
    nick_name      varchar(20)                            null,
    icon           varchar(255) default ''                null,
    create_time    datetime     default CURRENT_TIMESTAMP null,
    update_time    datetime     default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    is_deleted     int          default 0                 null comment '逻辑删除',
    student_id     varchar(30)  default ''                null comment '学号',
    public_key     varchar(256)                           null comment '公钥（与address相同）',
    address        varchar(256)                           null comment '区块链地址（公钥）',
    balance        bigint       default 100               null comment '信用积分余额',
    vip_level      int          default 0                 null comment 'VIP等级',
    fans_count     int          default 0                 null comment '粉丝数',
    follow_count   int          default 0                 null comment '关注数',
    last_sign_date date                                   null
);

create index idx_public_key
    on user (public_key);

create table user_account
(
    id          int auto_increment
        primary key,
    user_id     bigint                             not null comment '用户id',
    public_key  varchar(256)                       not null comment '公钥',
    money       bigint   default 0                 null comment '余额',
    is_deleted  int      default 0                 null comment '逻辑删除',
    creat_time  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint public_key
        unique (public_key),
    constraint user_id
        unique (user_id)
);

create table user_class
(
    id          bigint auto_increment
        primary key,
    user_id     bigint                             null comment '用户id',
    user_class  int                                null comment '用户会员等级 1 普通会员 2 高级会员 3 超级无敌至尊会员',
    creat_time  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    is_deleted  int      default 0                 null comment '逻辑删除',
    constraint user_id
        unique (user_id)
);

create table user_fans
(
    id          bigint auto_increment
        primary key,
    user_id     bigint                             null comment '被关注用户id',
    fan_id      bigint                             null comment '粉丝用户id',
    is_deleted  int      default 0                 null comment '逻辑删除',
    creat_time  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
);

create table user_follow
(
    id          bigint auto_increment
        primary key,
    user_id     bigint                             null comment '用户id',
    follow_id   bigint                             null,
    is_deleted  int      default 0                 null comment '逻辑删除',
    creat_time  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
);

create table user_like
(
    id       bigint auto_increment
        primary key,
    user_id  bigint            not null,
    post_id  bigint            not null,
    is_liked tinyint default 1 null,
    constraint uk_user_post
        unique (user_id, post_id)
);


