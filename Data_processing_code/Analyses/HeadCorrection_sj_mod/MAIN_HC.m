function [ wordPaths ] = MAIN_HC( path )
%MAIN_HC takes a head directory (.../Analyses/SingleFrames/) as input, searches through all subfolders (subjects), and runs Julia's headcorrection.m script on each subfolder
%   Sam Johnston
%   5-6-14
z=1;

%ask for user input to get the subject focders currently using
preSubjDirs = {};
while z > 0
    include = input('Which directories should be included?\nIf all, type "all"; otherwise type the two-digit identifier.\nWhen done, type "done"\nNB: you must use single quotes around ALL input!: ');
    class(include)
    if strcmp(include, 'all')
        someDirs = dir(path);
        otherDirs = [someDirs(:).isdir];
        preSubjDirs = {someDirs(otherDirs).name}';  %set cell array of all directories in path
        break

    elseif strcmp(include, 'done')
        break
        
    else
        preSubjDirs{z} = num2str(include);
    end
    z = z + 1;
end
            
preSubjDirs;


subjDirs = [];
%process the user input and ensure that there is only a list of numbers,
%(subj. folders), in case an accidental non-numerical input was entered.
for i = 1:length(preSubjDirs)
    if isnan(str2double(preSubjDirs(i)))
        continue
    end
    subjDirs = [ subjDirs preSubjDirs(i) ];  %create an array of only integer-name directories (subject numbers)
    continue
end

%convert empty list to cell array
subjPaths = [];
subjPaths = cell(length(subjDirs),1);

%concatenate the cwd with each of the subject directory names, resulting in
%an array of paths.
for i = 1:length(subjDirs)
    subjPaths{i} = strcat(path,subjDirs{i});  %create a cell array of the path for each of the subjects
end 

%class({subjPaths});

%obtain full paths for each repetition directory within subj. dir.
repPaths = [];
for i = 1:length(subjPaths)
    someDirs = dir(subjPaths{i});  %get dirs from within each subj. dir
    otherDirs = [someDirs(:).isdir];
    preRepDirs = {someDirs(otherDirs).name};
    preRepDirs(ismember(preRepDirs,{'.','..'})) = [];   %get rid of extra 'directories' that MATLAB things are directories
    for j = 1:length(preRepDirs)
        repPaths = [ repPaths strcat(subjPaths(i),'/',preRepDirs{j}) ];  % create vector of paths for each repetition for each subj
    end
end

% repPaths = {repPaths};  %create a cell array from the vector

%get paths to the word directories within each rep. directory
wordPaths = [];
for i = 1:length(repPaths)
    someDirs = dir(repPaths{i});
    otherDirs = [someDirs(:).isdir];
    preWordDirs = {someDirs(otherDirs).name};
    preWordDirs(ismember(preWordDirs,{'.','..'})) = [];
    for j = 1:length(preWordDirs)
        wordPaths = [ wordPaths strcat(repPaths(i),'/',preWordDirs{j})]; %create vector of paths for each word for each repetition
    end
end

wordPaths = {wordPaths}; %turn vector to cell array
disp(wordPaths)

disp(length(wordPaths{1}))
for i = 1:length(wordPaths{1})  %for each word-folder, run head_correction.m 
%     (this head correction.m file has been edited:uses program
%     calculations, and does not take user input)
    i = i;
    wordPaths{1}{i}
    try
        head_correction(wordPaths{1}{i});
%         input('Press enter to continue');
    catch exception
        disp('error encountered for:')
        disp(wordPaths{1}{i})
        disp(exception)
        continue
    end
end

end

