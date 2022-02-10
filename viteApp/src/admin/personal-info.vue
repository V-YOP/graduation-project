<template>
    <el-header>
        <h1 style="font-size:30px">个人信息</h1>
    </el-header>
    <el-main id="main">
        <el-space direction="vertical" alignment="left">
            <p>
                <span> 名称：{{ name || "LOADING" }}&nbsp;&nbsp; </span>
                <el-button type="text" @click="changeNameDialogVisible = true">更改</el-button>
            </p>
            <p>
                <el-button type="primary" @click="changePasswdDialogVisible = true"
                    >更改密码</el-button
                >
            </p>
        </el-space>
    </el-main>


    <el-dialog
  title="修改名称"
  v-model="changeNameDialogVisible"
  width="25%"
  append-to-body>
  新名称：<el-input v-model="newName"></el-input>
  <template #footer>
    <span class="dialog-footer">
      <el-button @click="changeNameDialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="toChangeName">确 定</el-button>
    </span>
  </template>
</el-dialog>

<el-dialog
  title="修改密码"
  v-model="changePasswdDialogVisible"
  width="25%"
  append-to-body
  >
  <el-space direction="vertical" alignment="left">
      <p>
  旧密码：<el-input v-model="oldPasswd"></el-input>

      </p>
       <p>
  新密码：<el-input v-model="newPasswd"></el-input>

      </p>
  </el-space>
  <template #footer>
    <span class="dialog-footer">
      <el-button @click="changePasswdDialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="toChangePasswd">确 定</el-button>
    </span>
  </template>
</el-dialog>
</template>



<script lang="ts">
import { ElMessage } from "element-plus";
import { defineComponent, PropType, ref, toRefs } from "vue";
import adminApi, { OptionList } from "../util/adminApi";
import { AllInfo } from "../util/api";
import Cookies from "js-cookie"
export default defineComponent({
    props: {
        info: Object as PropType<AllInfo>,
        optionList : Array as PropType<OptionList>
    },
    setup(props, context) {
        const loginData = adminApi.loginCheck()!;
        const {id, token} = loginData;

        const name = ref(loginData.name)
        const newName = ref(name.value)
        const oldPasswd = ref('')
        const newPasswd = ref('')
        const changeNameDialogVisible = ref(false)
        const changePasswdDialogVisible = ref(false)
        return {
            id,
            name : ref(name),
            token,
            changeName() {},
            changePasswd() {},
            changeNameDialogVisible,
            newName ,
            oldPasswd ,
            newPasswd,
            toChangeName(){
                let succ = ()=>{
                    name.value = newName.value;
                    ElMessage.success("更改成功")
                    Cookies.set("name", newName.value)
                }
                let fail = ()=>{
                    ElMessage.error("更改失败！")
                }
                adminApi.changeName(token, newName.value,succ,fail)
                changeNameDialogVisible.value = false;
            },
            toChangePasswd(){
                let succ = (newToken:string)=>{
                    ElMessage.success("密码修改成功！")
                    // TODO
                    Cookies.set("token", newToken)
                }
                let fail = ()=>{
                    ElMessage.error("密码修改失败！请检查输入密码是否正确！")
                }
                if (oldPasswd.value===newPasswd.value) {
                    ElMessage.error("新密码和旧密码不能相同！")
                    return;
                }
                adminApi.changePasswd(oldPasswd.value,newPasswd.value,succ,fail)
                changePasswdDialogVisible.value=  false;
            },
            changePasswdDialogVisible
        };
    },
});
</script>


<style scoped>
#main {
    margin-left: 50px;
    margin-top: 60px;
}
</style>