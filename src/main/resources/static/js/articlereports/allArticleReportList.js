document.addEventListener("DOMContentLoaded", function(){
	let report_line = document.getElementsByClassName("noti-table-line");
	
	//每列案件添加click事件
	for(let i = 0; i < report_line.length; i++){
		report_line[i].addEventListener("click", function(){
			let form = document.createElement("form");
			form.action = "/Articlereport/editArticleReportList";
			form.method = "post";
			
			let input = document.createElement("input");
			input.name = "artRepId";
			input.value = this.getAttribute("data-artRepId");
			form.appendChild(input);
			
			document.body.appendChild(form);
			form.submit();
		});
	}
	
	//計算儀表數據
	let sta = 0;
	for(let i = 0; i < report_line.length; i++){
		if(report_line[i].getAttribute("data-repSta") == 0){
			sta++;
		}
	}
	let staNum = document.getElementsByClassName("large-number");
	staNum[0].innerHTML = sta;
	
	//datatable
	let table = new DataTable("#admin-table");
	
	//成功案件修改訊息
	let successMsg = document.getElementById("successMsg");
	console.log(successMsg.value);
	if(successMsg.value != ""){
		alert(successMsg.value);	
	}
});