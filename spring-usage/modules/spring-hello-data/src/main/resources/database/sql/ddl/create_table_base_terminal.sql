create table base_terminal(
	terminal_no integer primary key identity,
	terminal_code varchar(20),
	line_no integer,
	modify_date datetime
);